package com.ivy.cpg.primarysale.view;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.primarysale.bo.DistInvoiceDetailsBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.Vector;

/**
 * Created by dharmapriya.k on 22-09-2015.
 */
public class InvoiceStatusFragment extends IvyBaseFragment {

    //private ArrayList<DistInvoiceDetailsBO> statusSelectedInv = new ArrayList<>();
    private BusinessModel bmodel;
    private DistInvoiceDetailsBO invoiceObj;
    private InvoiceDetailsAdapter mSchedule;
    private ListView invoice_listview;
    private Button btnSave;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_status, container,
                false);

        invoice_listview = (ListView) view.findViewById(R.id.invoice_listview);
        invoice_listview.setCacheColorHint(0);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        loadInvoiceDetails();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bmodel.disInvoiceDetailsHelper.hasStatus()) {
                    new SaveInvoiceStatus().execute();
                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_data_tosave), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public void loadInvoiceDetails() {
        try {
            mSchedule = new InvoiceDetailsAdapter(bmodel.disInvoiceDetailsHelper.getInvoices());
            invoice_listview.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        ((AppCompatActivity) getActivity()).getMenuInflater().inflate(
                R.menu.menu_invoice_status, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        int i = item.getItemId();
        if (i == android.R.id.home) {
            bmodel.distTimeStampHeaderHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
            getActivity().setResult(getActivity().RESULT_OK);
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_save) {
            if (bmodel.disInvoiceDetailsHelper.hasStatus()) {
                new SaveInvoiceStatus().execute();
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_data_tosave), Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, bmodel.reasonHelper.getPrimSaleReasonList());
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

    }

    private class InvoiceDetailsAdapter extends ArrayAdapter<DistInvoiceDetailsBO> {

        Vector<DistInvoiceDetailsBO> invoicelistitems;

        public InvoiceDetailsAdapter(Vector<DistInvoiceDetailsBO> invoicelistitems) {
            super(getActivity(), R.layout.fragment_invoice_status_list_item, invoicelistitems);
            this.invoicelistitems = invoicelistitems;
        }


        public DistInvoiceDetailsBO getItem(int position) {
            return invoicelistitems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return invoicelistitems.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            invoiceObj = invoicelistitems.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.fragment_invoice_status_list_item, null, false);
                holder = new ViewHolder();
                holder.invoice_no = (TextView) convertView.findViewById(R.id.invoice_no);
                holder.invoice_date = (TextView) convertView.findViewById(R.id.invoice_date);
                holder.invoice_value = (TextView) convertView.findViewById(R.id.invoice_value);
                holder.lpc = (TextView) convertView.findViewById(R.id.lpc);
                holder.statusspinner = (Spinner) convertView.findViewById(R.id.statusspinner);

                ((TextView) convertView.findViewById(R.id.tv_invoiceno_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                ((TextView) convertView.findViewById(R.id.startTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                ((TextView) convertView.findViewById(R.id.valueTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                ((TextView) convertView.findViewById(R.id.lpcTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                ((TextView) convertView.findViewById(R.id.statusTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.invoice_no.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.invoice_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.invoice_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.lpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.distInvoiceDetailsBO = invoiceObj;
            holder.invoice_no.setText(invoiceObj.getInvoiceId());
            holder.invoice_date.setText(invoiceObj.getDate());
            holder.invoice_value.setText(invoiceObj.getValue());
            holder.lpc.setText(invoiceObj.getLpc());
            holder.statusspinner.setAdapter(spinnerAdapter);

            if (holder.distInvoiceDetailsBO.getStatus() != null) {
                holder.statusspinner.setSelection(bmodel.reasonHelper.getSelectedPosition(holder.distInvoiceDetailsBO.getStatus()));
            }

            holder.statusspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

//                    if (statusSelectedInv.contains(holder.distInvoiceDetailsBO)) {
//                        statusSelectedInv.remove(holder.distInvoiceDetailsBO);
//
//                    }
                    if (holder.statusspinner.getSelectedItem().toString().equals(getResources().getString(R.string.select))) {

                        holder.distInvoiceDetailsBO.setStatus("0");
                        holder.distInvoiceDetailsBO.setUpload("X");
                        //statusSelectedInv.add(holder.distInvoiceDetailsBO);
                    } else {
                        ReasonMaster bo = (ReasonMaster) holder.statusspinner.getSelectedItem();
                        holder.distInvoiceDetailsBO.setStatus(bo.getReasonID());
                        holder.distInvoiceDetailsBO.setUpload("N");
                        //statusSelectedInv.add(holder.distInvoiceDetailsBO);
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            return convertView;
        }

        class ViewHolder {
            DistInvoiceDetailsBO distInvoiceDetailsBO;
            private TextView invoice_no, invoice_date, invoice_value, lpc;
            private Spinner statusspinner;
        }
    }

    private class SaveInvoiceStatus extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                bmodel.disInvoiceDetailsHelper.saveInvoiceDetails(bmodel.disInvoiceDetailsHelper.getInvoices());

            } catch (Exception e) {
                Commons.print("" + e);
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
        }
    }
}

