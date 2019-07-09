package com.ivy.cpg.view.profile.otpValidation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.NetworkUtils;
import com.ivy.utils.StringUtils;

public class OTPValidationDialog extends Dialog implements OnClickListener {

    private BusinessModel bmodel;

    private EditText password;
    private TextView messageTv;
    private Spinner reason;
    private ArrayAdapter<SpinnerBO> reasonAdapter;

    private AlertDialog alertDialog;
    private OnDismissListener otpPasswordDismissListener;

    private RetailerMasterBO mRetailerBO;
    private Context mContext;

    private OTPValidationHelper otpValidationHelper;
    private int actualRadius;


    public OTPValidationDialog(Context context,
                               OnDismissListener otpPasswordDismissListener,
                               RetailerMasterBO retailerBO, String strTitle, int actualRadius) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.otp_password_dialog);

        setCancelable(false);

        TextView titleBar = findViewById(R.id.titleBar);
        titleBar.setText(strTitle);

        this.otpPasswordDismissListener = otpPasswordDismissListener;
        otpValidationHelper = OTPValidationHelper.getInstance(context);

        mContext = context;
        this.bmodel = (BusinessModel)context.getApplicationContext();
        mRetailerBO = retailerBO;
        this.actualRadius = actualRadius;

        password = findViewById(R.id.passwordEditText);
        messageTv = findViewById(R.id.messageTv);
        reason = findViewById(R.id.reason);

        TextView contactnoTxtView = findViewById(R.id.contactnoTxtView);
        contactnoTxtView
                .setText(mContext.getResources().getString(
                        R.string.admin_contact_no)
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getAdminContactNo());

        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        contactnoTxtView.setWidth(outMetrics.widthPixels);

        //private ProgressDialog pd;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 1) {
            reason.setVisibility(View.VISIBLE);
            reasonAdapter = new ArrayAdapter<>(mContext,
                    R.layout.spinner_bluetext_layout);
            reasonAdapter.add(new SpinnerBO(0, context.getResources()
                    .getString(R.string.select_reason)));
            loadOTPReason();
            reasonAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            reason.setAdapter(reasonAdapter);

        } else {
            password.setVisibility(View.VISIBLE);
        }

        password.setRawInputType(Configuration.KEYBOARD_12KEY);

        Button ok = findViewById(R.id.btn_ok);
        Button cancel = findViewById(R.id.btn_cancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);


        customProgressDialog(builder);
        alertDialog = builder.create();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_ok) {
            if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 1)
                if (((SpinnerBO) reason.getSelectedItem()).getId() != 0) {
                    new CheckOTPPassword(password.getText().toString(), ((SpinnerBO) reason.getSelectedItem()).getId()).execute();

                } else {
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.select_reason), Toast.LENGTH_SHORT)
                            .show();
                }
            else {
                if (StringUtils.isNullOrEmpty(password.getText().toString())) {
                    messageTv.setText(mContext.getResources().getString(
                            R.string.enter_otp));
                } else {
                    if (!NetworkUtils.isNetworkConnected(mContext))
                        Toast.makeText(
                                mContext,
                                mContext.getResources().getString(
                                        R.string.no_network_connection),
                                Toast.LENGTH_SHORT).show();
                    else
                        new CheckOTPPassword(password.getText().toString(), 0).execute();


                }
            }

        } else if (i == R.id.btn_cancel) {
            this.cancel();

        }
    }

    private void doDismiss() {
        this.otpPasswordDismissListener.onDismiss(this);
    }

    private void doCancel() {
        password.setText("");
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    private void loadOTPReason() {
        try {
            SpinnerBO reason;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(StandardListMasterConstants.OTP_SKIP_REASON_TYPE));
            if (c != null) {
                while (c.moveToNext()) {
                    reason = new SpinnerBO(c.getInt(0), c.getString(1));
                    reasonAdapter.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class CheckOTPPassword extends AsyncTask<Void, Void, String> {

        String otp;
        int reasonID;

        CheckOTPPassword(String otp, int reasonID) {
            this.otp =  otp;
            this.reasonID = reasonID;
        }

        protected void onPreExecute() {
            alertDialog.show();
            messageTv.setText("");
        }

        protected void onPostExecute(String result) {

            if (alertDialog != null)
                alertDialog.dismiss();
            switch (result) {
                case "1":
                    if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 0) {
                        mRetailerBO.setOtpActivatedDate(DateTimeUtils
                                .now(DateTimeUtils.DATE_GLOBAL));
                        otpValidationHelper.saveOTPActivatedDate(mRetailerBO.getRetailerID(), 1);
                    }
                    doDismiss();
                    break;
                case "-1":
                    messageTv.setText(mContext.getResources().getString(
                            R.string.otp_expired));
                    break;
                case "-2":
                    messageTv.setText(mContext.getResources().getString(
                            R.string.invalid_otp));
                    break;
                case "-3":
                    messageTv.setText(mContext.getResources().getString(
                            R.string.activated_already));
                    break;
                case "-4":
                    messageTv.setText(mContext.getResources().getString(
                            R.string.activation_error_try_again));
                    break;
                case "-5":
                    messageTv.setText(mContext.getResources().getString(R.string.session_expired));
                    break;
            }
            doCancel();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 1) {
                    return otpValidationHelper.saveOTPSkipReason(reasonID, mRetailerBO, "REASON", actualRadius);
                }
                else {
                    String sucessStaus = bmodel
                            .checkOTP(
                                    mRetailerBO.getRetailerID(),
                                    otp,
                                    bmodel.getStandardListIdAndType(
                                            StandardListMasterConstants.SLM_RET_GPS_CODE,
                                            StandardListMasterConstants.OTP_LIST_CODE));
                    if ("1".equals(sucessStaus))
                        otpValidationHelper.saveOTPSkipReason(reasonID, mRetailerBO,"OTP", actualRadius);
                    return sucessStaus;
                }
            } catch (Exception e) {
                return "0";
            }
        }
    }

    private void customProgressDialog(AlertDialog.Builder builder) {

        try {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_alert_dialog,
                    findViewById(R.id.layout_root));

            TextView title = layout.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            TextView messagetv = layout.findViewById(R.id.text);
            messagetv.setText(getOwnerActivity().getResources().getString(R.string.loading));

            builder.setView(layout);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
