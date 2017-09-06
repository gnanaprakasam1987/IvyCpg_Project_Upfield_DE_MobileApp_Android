package com.ivy.sd.png.view;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerContractBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RenewContractDialog extends DialogFragment {

    private BusinessModel bmodel;
    View v;
    private String contractId, outDateFormat;
    private TextView tvCancel, tvOk;
    private RetailerContractBO mretailerContractBO;
    private EditText etStartDate, etEndDate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        v = inflater.inflate(R.layout.dialog_renew_contract, container, false);

        contractId = getArguments().getString("ContractID");

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        tvCancel = (TextView) v.findViewById(R.id.tv_cancel);
        tvOk = (TextView) v.findViewById(R.id.tv_ok);
        etStartDate = (EditText) v.findViewById(R.id.et_startdate);
        etEndDate = (EditText) v.findViewById(R.id.et_enddate);

        tvCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        tvOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etEndDate.getText().toString().length() > 0) {
                    mretailerContractBO.setStartdate(etStartDate.getText().toString());
                    mretailerContractBO.setEnddate(etEndDate.getText().toString());
                    new SaveAsyncTask().execute();
                } else
                    Toast.makeText(getActivity(), getResources().getString(R.string.alert_select_date), Toast.LENGTH_LONG).show();
            }
        });

        for (RetailerContractBO retailer : bmodel.retailerContractHelper.getRetailerContractList()) {
            if (retailer.getContractid().equals(contractId))
                mretailerContractBO = retailer;
        }
        outDateFormat = "yyyy/MM/dd";

        etStartDate.setText(getNextDate(mretailerContractBO.getEnddate()));

        etEndDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar();
            }
        });


        return v;
    }

    public String getNextDate(String curDate) {
        String nextDate = "";
        try {
            final SimpleDateFormat format = new SimpleDateFormat(outDateFormat);
            final Date date = format.parse(curDate);
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            nextDate = format.format(calendar.getTime());
        } catch (Exception e) {
            nextDate = "";

        }
        return nextDate;

    }


    public long getNextDateMills(String curDate) {
        long nextDate;
        try {
            final SimpleDateFormat format = new SimpleDateFormat(outDateFormat);
            final Date date = format.parse(curDate);
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, 2);

            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));

            nextDate = calendar.getTimeInMillis();
        } catch (Exception e) {
            nextDate = 0;

        }
        return nextDate;

    }


    public boolean isShowing() {
        if (getDialog() != null) {
            return true;
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }

        int dialogHeight = (int) getActivity().getResources().getDimension(R.dimen.dialog_height); // specify a value here

        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);


    }

    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    private void showCalendar() {
        final Calendar c1 = Calendar.getInstance();
        int mToYear = c1.get(Calendar.YEAR);
        int mToMonth = c1.get(Calendar.MONTH);
        int mToDay = c1.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd1 = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        etEndDate
                                .setText(year
                                        + "/"
                                        + ((monthOfYear + 1) < 10 ? "0"
                                        + (monthOfYear + 1)
                                        : (monthOfYear + 1))
                                        + "/"
                                        + ((dayOfMonth) < 10 ? "0"
                                        + (dayOfMonth)
                                        : (dayOfMonth)));

                    }
                }, mToYear, mToMonth, mToDay);

        dpd1.getDatePicker().setCalendarViewShown(false);

        dpd1.getDatePicker().setMinDate(getNextDateMills(mretailerContractBO.getEnddate()));

        dpd1.show();
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.retailerContractHelper.saveRetailersContract(mretailerContractBO);
                bmodel.retailerContractHelper.downloadRenewedContract(mretailerContractBO.getRetailerid());
                bmodel.retailerContractHelper.downloadRetailerContract(mretailerContractBO.getRetailerid());
                return Boolean.TRUE;
            } catch (Exception e) {
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();

            Toast.makeText(getActivity(),
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            getDialog().dismiss();


        }

    }

}
