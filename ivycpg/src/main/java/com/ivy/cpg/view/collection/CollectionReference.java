package com.ivy.cpg.view.collection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.CaptureSignatureActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;

public class CollectionReference extends IvyBaseActivityNoActionBar {
    private static final String TAG = "CollectionReference";
    private static BusinessModel bmodel;
    private ArrayList<InvoiceHeaderBO> mInvioceList;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private String mErrorMsg = "";
    private String mSelectedBill = "";
    private MyAdapter mCollectionAdapter;
    private static final int REQUEST_SIGNAATURE_CAPTURE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_reference);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        bmodel.configurationMasterHelper.checkCollectionDocConfig();
        initializeItems();

    }

    private void initializeItems() {

        String title = bmodel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_COLLECTION_REF");

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Set title to actionbar
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(title);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            getSupportActionBar().setDisplayUseLogoEnabled(false);

        }

        //Set Data for Spinner Adapter
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        ReasonMaster reasonBO;
        reasonBO = new ReasonMaster();
        reasonBO.setReasonID("-1");
        reasonBO.setReasonDesc(getResources().getString(R.string.select_reason));
        reasonBO.setReasonCategory("NONE");
        spinnerAdapter.add(reasonBO);
        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("INVT"))
                spinnerAdapter.add(temp);
        }
        reasonBO = new ReasonMaster();
        reasonBO.setReasonID("0");
        reasonBO.setReasonDesc(getResources().getString(R.string.other_reason));
        reasonBO.setReasonCategory("NONE");
        spinnerAdapter.add(reasonBO);


        ListView mCollectionLV = findViewById(R.id.list);
        Button btnSave = findViewById(R.id.btn_save);

        mInvioceList = new ArrayList<>();

        if (bmodel.getInvoiceHeaderBO() != null && bmodel.getInvoiceHeaderBO().size() > 0) {
            for (InvoiceHeaderBO invoiceHeaderBO : bmodel.getInvoiceHeaderBO()) {
                if (invoiceHeaderBO.getBalance() > 0) {
                    mInvioceList.add(invoiceHeaderBO);
                }
            }
            if (mInvioceList != null && mInvioceList.size() > 0) {
                mCollectionAdapter = new MyAdapter();
                mCollectionLV.setAdapter(mCollectionAdapter);
                btnSave.setEnabled(true);
            } else {
                btnSave.setEnabled(false);
            }
        } else {
            btnSave.setEnabled(false);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidate())
                    new SaveAsyncTask().execute();
                else
                    Toast.makeText(CollectionReference.this, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isValidate() {
        for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
            if (invoiceHeaderBO.getDocExchange() == 0 && invoiceHeaderBO.getDocReasonId().equals("-1")) {
                mErrorMsg = "Kindly " + getResources().getString(R.string.select_reason);
                return false;
            }
            if (invoiceHeaderBO.getDocExchange() == 0 && invoiceHeaderBO.getDocReasonId().equals("0") && invoiceHeaderBO.getDocRemark().length() == 0) {
                mErrorMsg = getResources().getString(R.string.enter_remarks);
                return false;
            }
            if (invoiceHeaderBO.getDocExchange() == 1 && invoiceHeaderBO.getDocRefNo().length() == 0) {
                mErrorMsg = getResources().getString(R.string.enter_docref_number);
                return false;
            }
            if (invoiceHeaderBO.getDocExchange() == 1 && invoiceHeaderBO.getDocSignPath().length() == 0) {
                mErrorMsg = getResources().getString(R.string.get_signature);
                return false;
            }
            if (bmodel.configurationMasterHelper.IS_DOC_SIGN) {
                if (invoiceHeaderBO.getDocExchange() == 1 && invoiceHeaderBO.getDocSignPath().length() == 0) {
                    mErrorMsg = getResources().getString(R.string.get_signature);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mInvioceList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.list_item_collec_ref,
                        parent, false);
                holder = new ViewHolder();
                holder.tvInvoiceNo = row.findViewById(R.id.tv_invoice_no);
                holder.invAmt = row.findViewById(R.id.tvinvamtValue);
                holder.balanceAmt = row.findViewById(R.id.tvbalamtValue);
                holder.paidAmt = row.findViewById(R.id.tvpaidamtValue);
                holder.etDocRef = row.findViewById(R.id.etdocRef);
                holder.etContactName = row.findViewById(R.id.etcontactName);
                holder.etContactNo = row.findViewById(R.id.etcontactNo);
                holder.cbDocExchange = row.findViewById(R.id.cbDocExchange);
                holder.spReason = row.findViewById(R.id.spreason);
                holder.etRemark = row.findViewById(R.id.etremark);
                holder.llDocReason = row.findViewById(R.id.lldocReason);
                holder.llDocRemark = row.findViewById(R.id.lldocRemark);
                holder.ivSignature = row.findViewById(R.id.ivSign);

                holder.etDocRef.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.etDocRef.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setDocRefNo(qty);
                        } else {
                            holder.invoiceHeaderBO.setDocRefNo("");
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                holder.etContactName.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.etContactName.setSelection(qty.length());
                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setContactName(qty);
                        } else {
                            holder.invoiceHeaderBO.setContactName("");
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                holder.etContactNo.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.etContactNo.setSelection(qty.length());
                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setContactNo(qty);
                        } else {
                            holder.invoiceHeaderBO.setContactNo("");
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                holder.etRemark.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.etRemark.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setDocRemark(qty);
                        } else {
                            holder.invoiceHeaderBO.setDocRemark("");
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                holder.spReason.setAdapter(spinnerAdapter);
                holder.spReason
                        .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(
                                    AdapterView<?> parent, View view,
                                    int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.spReason
                                        .getSelectedItem();

                                holder.invoiceHeaderBO.setDocReasonId(reString
                                        .getReasonID());

                                if (reString.getReasonID().equals("0")) {
                                    holder.llDocRemark.setVisibility(View.VISIBLE);
                                    holder.etRemark.setText("");
                                } else {
                                    holder.llDocRemark.setVisibility(View.GONE);
                                }

                            }

                            public void onNothingSelected(
                                    AdapterView<?> parent) {
                            }
                        });

                holder.cbDocExchange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            holder.llDocReason.setVisibility(View.GONE);
                            holder.llDocRemark.setVisibility(View.GONE);
                            holder.invoiceHeaderBO.setDocExchange(1);
                            holder.etContactName.setEnabled(true);
                            holder.etContactNo.setEnabled(true);
                            holder.etDocRef.setEnabled(true);
                            holder.invoiceHeaderBO.setDocReasonId("");
                            holder.invoiceHeaderBO.setDocRemark("");
                        } else {
                            holder.llDocReason.setVisibility(View.VISIBLE);
                            holder.spReason.setSelection(0);
                            holder.invoiceHeaderBO.setDocExchange(0);
                        }

                    }
                });

                holder.ivSignature.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectedBill = holder.invoiceHeaderBO.getInvoiceNo();
                        if (holder.invoiceHeaderBO.getDocSignImage() != null && holder.invoiceHeaderBO.getDocSignImage().length() > 0) {
                            if (bmodel.checkForNFilesInFolder(FileUtils.photoFolderPath, 1, holder.invoiceHeaderBO.getDocSignImage())) {
                                DialogFragment dialog = new signatureExistingAlert();
                                Bundle args = new Bundle();
                                args.putString("title", getResources().getString(
                                        R.string.word_photocaptured_delete_retake));
                                args.putString("imgName", holder.invoiceHeaderBO.getDocSignImage());
                                dialog.setArguments(args);
                                dialog.show(getSupportFragmentManager(), "sign");

                            } else {

                                Intent i = new Intent(CollectionReference.this,
                                        CaptureSignatureActivity.class);
                                i.putExtra("fromModule", "COL_REF");
                                startActivityForResult(i, REQUEST_SIGNAATURE_CAPTURE);
                                bmodel.configurationMasterHelper.setSignatureTitle("Signature");
                            }
                        } else {
                            Intent i = new Intent(CollectionReference.this,
                                    CaptureSignatureActivity.class);
                            i.putExtra("fromModule", "COL_REF");
                            startActivityForResult(i, REQUEST_SIGNAATURE_CAPTURE);
                            bmodel.configurationMasterHelper.setSignatureTitle("Signature");
                        }
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.invoiceHeaderBO = mInvioceList.get(position);

            holder.tvInvoiceNo.setText(holder.invoiceHeaderBO.getInvoiceNo());
            holder.invAmt.setText(bmodel.formatValue(holder.invoiceHeaderBO.getInvoiceAmount()));
            holder.balanceAmt.setText(bmodel.formatValue(holder.invoiceHeaderBO.getBalance()));
            holder.paidAmt.setText(bmodel.formatValue(holder.invoiceHeaderBO.getPaidAmount()));
            holder.etDocRef.setText(holder.invoiceHeaderBO.getDocRefNo());
            holder.etContactName.setText(holder.invoiceHeaderBO.getContactName());
            holder.etContactNo.setText(holder.invoiceHeaderBO.getContactNo());
            holder.cbDocExchange.setChecked(holder.invoiceHeaderBO.getDocExchange() == 1);

            if (holder.invoiceHeaderBO.getDocReasonId() != null) {
                holder.spReason.setSelection(getReasonIndex(holder.invoiceHeaderBO.getDocReasonId()));
            }
            holder.etRemark.setText(holder.invoiceHeaderBO.getDocRemark());
            if (holder.invoiceHeaderBO.getDocSignImage() != null && holder.invoiceHeaderBO.getDocSignImage().length() > 0) {
                holder.ivSignature.setColorFilter(ContextCompat.getColor(CollectionReference.this, R.color.green_productivity), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            if (!bmodel.configurationMasterHelper.IS_DOC_SIGN) {
                holder.ivSignature.setVisibility(View.GONE);
            }

            if (!bmodel.configurationMasterHelper.IS_DOC_REFNO) {
                holder.etDocRef.setFocusable(false);
                holder.etDocRef.setEnabled(false);
                holder.etDocRef.setCursorVisible(false);
                holder.etDocRef.setKeyListener(null);
                holder.etDocRef.setBackgroundColor(Color.TRANSPARENT);
            }

            return row;
        }
    }

    class ViewHolder {
        TextView tvInvoiceNo;
        TextView invAmt;
        TextView balanceAmt;
        TextView paidAmt;
        EditText etDocRef;
        EditText etContactName;
        EditText etContactNo;
        CheckBox cbDocExchange;
        Spinner spReason;
        EditText etRemark;
        LinearLayout llDocReason;
        LinearLayout llDocRemark;
        ImageView ivSignature;

        InvoiceHeaderBO invoiceHeaderBO;
    }

    @SuppressLint("StaticFieldLeak")
    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                CollectionHelper.getInstance(CollectionReference.this).saveCollectionReference(mInvioceList);
                bmodel.saveModuleCompletion("MENU_COLLECTION_REF", true);
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(CollectionReference.this,
                    DataMembers.SD, getResources().getString(R.string.saving),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));

            Toast.makeText(CollectionReference.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            onBackClicked();

        }

    }

    private void onBackClicked() {
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                .now(DateTimeUtils.TIME));
        finish();

        Intent myIntent = new Intent(this, HomeScreenTwo.class);
        startActivityForResult(myIntent, 0);

        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public int getReasonIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Commons.print(TAG + ",onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_SIGNAATURE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
                        if (mSelectedBill.equals(invoiceHeaderBO.getInvoiceNo())) {
                            invoiceHeaderBO.setDocSignPath(data.getStringExtra("SERVER_PATH"));
                            invoiceHeaderBO.setDocSignImage(data.getStringExtra("IMAGE_NAME"));
                            mCollectionAdapter.notifyDataSetChanged();
                            break;
                        }

                    }
                }
                break;
        }
    }

    @SuppressLint("ValidFragment")
    public static class signatureExistingAlert extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //   return super.onCreateDialog(savedInstanceState);

            Bundle args = getArguments();
            String title = args.getString("title");
            final String imgName = args.getString("imgName");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.deleteFiles(FileUtils.photoFolderPath,
                                    imgName);
                            Intent i = new Intent(getActivity(),
                                    CaptureSignatureActivity.class);
                            i.putExtra("fromModule", "COL_REF");
                            startActivityForResult(i, REQUEST_SIGNAATURE_CAPTURE);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();
        }
    }

}
