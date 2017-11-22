package com.ivy.sd.png.view.van;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class ManualVanLoadBatchEntryDialog extends Dialog implements
        OnClickListener {
    private static Button mfg_date;
    private static Button exp_date;
    private static LoadManagementBO product;
    private static String outPutDateFormat;
    private BusinessModel bmodel;
    private OnDismissListener addBatch, cancelBatch;
    private Activity activity;
    private EditText batch_no;
    private FragmentManager fragmentManager;
    private TextView messagetv;

    ManualVanLoadBatchEntryDialog(Activity activity,
                                  LoadManagementBO productBO, OnDismissListener addBatch,
                                  OnDismissListener cancelBatch, FragmentManager fragmentManager) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.activity = activity;
        this.addBatch = addBatch;
        this.cancelBatch = cancelBatch;
        this.fragmentManager = fragmentManager;
        product = productBO;
        setContentView(R.layout.dialog_manual_vanload_batch_entry);
        setCancelable(true);
        bmodel = (BusinessModel) activity.getApplicationContext();
        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;
        TextView product_name = (TextView) findViewById(R.id.product_name);
        product_name
                .setText(product.getProductshortname() + " [" + product.getProductid() + "]");
        batch_no = (EditText) findViewById(R.id.batch_no);
        Button add = (Button) findViewById(R.id.add);
        add.setOnClickListener(this);
        Button close = (Button) findViewById(R.id.close);
        close.setOnClickListener(this);
        mfg_date = (Button) findViewById(R.id.mfg_date);
        mfg_date.setText((DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat)));
        product.setMfgDate(mfg_date.getText().toString());
        mfg_date.setOnClickListener(this);
        exp_date = (Button) findViewById(R.id.exp_date);
        exp_date.setText((DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat)));
        product.setExpDate(exp_date.getText().toString());
        exp_date.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.add) {

            if (batch_no.getText() != null && batch_no.getText().length() > 0) {

                product.setManualBatchNo(batch_no.getText().toString());
                if (product.getBatchlist() == null) {

                    new SaveBatch().execute();
                } else {
                    boolean ishit = false;
                    if (product.getBatchlist().get(0).getBatchnolist() != null) {
                        for (LoadManagementBO batchNo : product.getBatchlist()
                                .get(0).getBatchnolist()) {
                            if (batchNo.getBatchNo().equals(
                                    product.getManualBatchNo())) {
                                ishit = true;
                                break;
                            }
                        }
                    }

                    if (!ishit) {
                        product.setManualBatchNo(batch_no.getText().toString());
                        new SaveBatch().execute();
                    } else {
                        Toast.makeText(
                                activity,
                                activity.getResources().getString(
                                        R.string.batch_no_already_exist),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                batch_no.setText("");
                product.setManualBatchNo("");
                product.setMfgDate("");
                product.setExpDate("");
                Toast.makeText(
                        activity,
                        activity.getResources().getString(
                                R.string.please_fill_all_mandatory_fields),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.close) {
            cancelBatch.onDismiss(ManualVanLoadBatchEntryDialog.this);
        } else if (id == R.id.mfg_date) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(fragmentManager, "datePicker1");
        } else if (id == R.id.exp_date) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(fragmentManager, "datePicker2");
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            if (this.getTag().equals("datePicker1")) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    product.setMfgDate(DateUtil.convertDateObjectToRequestedFormat(
                            Calendar.getInstance().getTime(), outPutDateFormat));
                    mfg_date.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    product.setMfgDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    mfg_date.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else if (this.getTag().equals("datePicker2")) {
                if (product.getMfgDate() != null
                        && product.getMfgDate().length() > 0) {
                    Date dateMfg = DateUtil.convertStringToDateObject(
                            product.getMfgDate(), outPutDateFormat);
                    if (dateMfg != null && selectedDate.getTime() != null
                            && dateMfg.after(selectedDate.getTime())) {
                        Toast.makeText(getActivity(),
                                R.string.expdate_set_after_mfgdate,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        product.setExpDate(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        exp_date.setText(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                } else {
                    product.setExpDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    exp_date.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            }
        }
    }

    private class SaveBatch extends AsyncTask<Integer, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(activity);
            customProgressDialog(builder);
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.vanmodulehelper.saveBatch(product);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            try {
                alertDialog.dismiss();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            batch_no.setText("");
            addBatch.onDismiss(ManualVanLoadBatchEntryDialog.this);

        }

    }

    private void customProgressDialog(AlertDialog.Builder builder) {

        try {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_alert_dialog,
                    (ViewGroup) activity.findViewById(R.id.layout_root));

            TextView title = (TextView) layout.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            messagetv = (TextView) layout.findViewById(R.id.text);
            messagetv.setText(activity.getResources().getString(R.string.loading));

            builder.setView(layout);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
