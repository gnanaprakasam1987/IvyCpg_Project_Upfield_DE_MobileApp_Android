package com.ivy.sd.png.view;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

public class CollectionReference extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private Toolbar toolbar;
    private ArrayList<InvoiceHeaderBO> mInvioceList;
    private ListView mCollectionLV;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_reference);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        initializeItems();

    }

    private void initializeItems() {

        String title = bmodel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_COLLECTION_REF");

        toolbar = (Toolbar) findViewById(R.id.toolbar);

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

        mCollectionLV = (ListView) findViewById(R.id.lvwplist);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mInvioceList = new ArrayList<>();

        if (bmodel.getInvoiceHeaderBO() != null && bmodel.getInvoiceHeaderBO().size() > 0) {
            for (InvoiceHeaderBO invoiceHeaderBO : bmodel.getInvoiceHeaderBO()) {
                if (invoiceHeaderBO.getBalance() > 0) {
                    mInvioceList.add(invoiceHeaderBO);
                }
            }
            if (mInvioceList != null && mInvioceList.size() > 0) {
                MyAdapter mCollectionAdapter = new MyAdapter();
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
                new SaveAsyncTask().execute();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            finish();
            BusinessModel.loadActivity(this,
                    DataMembers.actHomeScreenTwo);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
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
                holder.tvInvoiceNo = (TextView) row.findViewById(R.id.tv_invoice_no);
                holder.invAmt = (TextView) row.findViewById(R.id.tvinvamtValue);
                holder.balanceAmt = (TextView) row.findViewById(R.id.tvbalamtValue);
                holder.paidAmt = (TextView) row.findViewById(R.id.tvpaidamtValue);
                holder.etDocRef = (EditText) row.findViewById(R.id.etdocRef);
                holder.etContactName = (EditText) row.findViewById(R.id.etcontactName);
                holder.etContactNo = (EditText) row.findViewById(R.id.etcontactNo);

                ((TextView) row.findViewById(R.id.tvinvamt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.tvpaidamt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.tvbalamt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.docRefTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.contactNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.contactNoTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvInvoiceNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.invAmt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.balanceAmt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.paidAmt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etDocRef.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etContactName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etContactNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


                holder.etDocRef.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setDocRefNo(qty);
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
                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setContactName(qty);
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
                        if (!"".equals(qty)) {
                            holder.invoiceHeaderBO.setContactNo(qty);
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

        InvoiceHeaderBO invoiceHeaderBO;
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.collectionHelper.saveCollectionReference(mInvioceList);
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

            Toast.makeText(CollectionReference.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();


        }

    }
}
