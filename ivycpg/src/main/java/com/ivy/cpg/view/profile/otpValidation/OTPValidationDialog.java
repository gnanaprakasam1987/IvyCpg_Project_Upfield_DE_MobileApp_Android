package com.ivy.cpg.view.profile.otpValidation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
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
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.AppUtils;

public class OTPValidationDialog extends Dialog implements OnClickListener {

    private BusinessModel bmodel;

    private EditText password;
    private TextView messageTv;
    private Spinner reason;
    private ArrayAdapter<SpinnerBO> reasonAdapter;

    private AlertDialog alertDialog;
    private OnDismissListener otpPasswordDismissListener;

    private RetailerMasterBO mRetailerBO;
    private FragmentActivity activityCtxt;

    private OTPValidationHelper otpValidationHelper;

    private ValidationType flag;

    public enum ValidationType {

        LOCATION(1),
        WALKING_SEQ(2);

        private int value;

        ValidationType(int value) {
            this.value = value;
        }
    }


    public OTPValidationDialog(FragmentActivity activity, BusinessModel bmodel,
                               OnDismissListener otpPasswordDismissListener,
                               RetailerMasterBO retailerBO, String strTitle, ValidationType flag) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.otp_password_dialog);

        TextView titleBar = findViewById(R.id.titleBar);
        titleBar.setText(strTitle);

        this.otpPasswordDismissListener = otpPasswordDismissListener;
        otpValidationHelper = new OTPValidationHelper(activity.getApplicationContext());

        activityCtxt = activity;
        this.bmodel = bmodel;
        this.flag = flag;
        mRetailerBO = retailerBO;

        password = findViewById(R.id.passwordEditText);
        messageTv = findViewById(R.id.messageTv);
        reason = findViewById(R.id.reason);

        TextView contactnoTxtView = findViewById(R.id.contactnoTxtView);
        contactnoTxtView
                .setText(activityCtxt.getResources().getString(
                        R.string.admin_contact_no)
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getAdminContactNo());

        DisplayMetrics outMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        contactnoTxtView.setWidth(outMetrics.widthPixels);
        //	pd = new ProgressDialog(activityCtxt);

        titleBar.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        contactnoTxtView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        messageTv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        //private ProgressDialog pd;
        AlertDialog.Builder builder = new AlertDialog.Builder(activityCtxt);

        if ((flag == ValidationType.WALKING_SEQ) && bmodel.configurationMasterHelper.ret_skip_flag == 2) {
            reason.setVisibility(View.VISIBLE);
            reasonAdapter = new ArrayAdapter<SpinnerBO>(activityCtxt,
                    R.layout.spinner_bluetext_layout);
            reasonAdapter.add(new SpinnerBO(0, activity.getResources()
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
            if ((flag == ValidationType.WALKING_SEQ) && bmodel.configurationMasterHelper.ret_skip_flag == 2)
                // to do
                if (((SpinnerBO) reason.getSelectedItem()).getId() != 0) {
                    new CheckOTPPassword().execute();

                } else {
                    Toast.makeText(
                            activityCtxt,
                            activityCtxt.getResources().getString(
                                    R.string.select_reason), Toast.LENGTH_SHORT)
                            .show();
                }
            else {
                if (password.getText() == null
                        || password.getText().toString().equals("")) {
                    messageTv.setText(activityCtxt.getResources().getString(
                            R.string.enter_otp));
                } else {
                    if (!bmodel.isOnline())
                        Toast.makeText(
                                activityCtxt,
                                activityCtxt.getResources().getString(
                                        R.string.no_network_connection),
                                Toast.LENGTH_SHORT).show();
                    else
                        new CheckOTPPassword().execute();


                }
            }

        } else if (i == R.id.btn_cancel) {
            this.cancel();

        }
    }

    public void doDismiss() {
        this.otpPasswordDismissListener.onDismiss(this);
    }

    public void doCancel() {
        password.setText("");
       /* if (pd != null && pd.isShowing())
            pd.dismiss();*/
        if (alertDialog != null)
            alertDialog.dismiss();
        // this.cancel();
    }

    public void loadOTPReason() {
        try {
            SpinnerBO reason;
            DBUtil db = new DBUtil(activityCtxt, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(StandardListMasterConstants.OTP_REASON_TYPE));
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

        protected void onPreExecute() {
            /*if (pd != null && !pd.isShowing())
				pd.show();*/
            alertDialog.show();
            messageTv.setText("");
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
           /* if (pd != null && pd.isShowing())
                pd.dismiss();*/
            if (alertDialog != null)
                alertDialog.dismiss();
            switch (result) {
                case "1":
                    if (flag == ValidationType.LOCATION)
                        mRetailerBO.setOtpActivatedDate(SDUtil
                                .now(SDUtil.DATE_GLOBAL));
                    else if (bmodel.configurationMasterHelper.ret_skip_flag == 2
                            || flag == ValidationType.WALKING_SEQ) {
                        mRetailerBO.setSkipActivatedDate(SDUtil
                                .now(SDUtil.DATE_GLOBAL));
                        mRetailerBO.setSkip(true);
                    }
                    otpValidationHelper.saveOTPActivatedDate(mRetailerBO.getRetailerID(), flag.value);
                    doDismiss();
                    break;
                case "-1":
                    messageTv.setText(activityCtxt.getResources().getString(
                            R.string.otp_expired));
                    break;
                case "-2":
                    messageTv.setText(activityCtxt.getResources().getString(
                            R.string.invalid_otp));
                    break;
                case "-3":
                    messageTv.setText(activityCtxt.getResources().getString(
                            R.string.activated_already));
                    break;
                case "-4":
                    messageTv.setText(activityCtxt.getResources().getString(
                            R.string.activation_error_try_again));
                    break;
                case "-5":
                    messageTv.setText("Session Expired ");
                    break;
            }
            doCancel();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                if (bmodel.configurationMasterHelper.ret_skip_flag == 2)
                    return saveReason();
                else if (1 == ValidationType.LOCATION.value) // For Retailer GPS OTP
                    return bmodel
                            .checkOTP(
                                    mRetailerBO.getRetailerID(),
                                    password.getText().toString(),
                                    bmodel.getStandardListIdAndType(
                                            StandardListMasterConstants.SLM_RET_GPS_CODE,
                                            StandardListMasterConstants.OTP_LIST_CODE));
                else if (ValidationType.WALKING_SEQ.value == 2) // For Retailer Skip OTP
                    return bmodel
                            .checkOTP(
                                    mRetailerBO.getRetailerID(),
                                    password.getText().toString(),
                                    bmodel.getStandardListIdAndType(
                                            StandardListMasterConstants.SLM_RET_SEQ_CODE,
                                            StandardListMasterConstants.OTP_LIST_CODE));
            } catch (Exception e) {
                return "0";
            }
            return "0";
        }

        private String saveReason() {

            try {
                DBUtil db = new DBUtil(activityCtxt, DataMembers.DB_NAME
                );
                String values;
                db.createDataBase();
                db.openDataBase();

                String id;

                String columns = "UID,RetailerID,RouteID,Date,ReasonID,ReasonTypes,upload,DistributorID,ridSF";

                id = AppUtils.QT(bmodel.getAppDataProvider().getUser()
                        .getDistributorid()
                        + ""
                        + bmodel.getAppDataProvider().getUser().getUserid()
                        + "" + SDUtil.now(SDUtil.DATE_TIME_ID));

                values = id
                        + ","
                        + AppUtils.QT(mRetailerBO.getRetailerID())
                        + ","
                        + mRetailerBO.getBeatID()
                        + ","
                        + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + ","
                        + ((SpinnerBO) reason.getSelectedItem()).getId()
                        + ","
                        + AppUtils.QT(bmodel
                        .getStandardListId(StandardListMasterConstants.OTP_REASON_TYPE))
                        + "," + AppUtils.QT("N")
                        + "," + mRetailerBO.getDistributorId()
                        + "," + AppUtils.QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF());

                db.deleteSQL(
                        "Nonproductivereasonmaster",
                        "RetailerID="
                                + AppUtils.QT(mRetailerBO.getRetailerID())
                                + " and DistributorId="
                                + mRetailerBO.getDistributorId()
                                + " and ReasonTypes="
                                + AppUtils.QT(bmodel
                                .getStandardListId(StandardListMasterConstants.OTP_REASON_TYPE))
                                + " and Date="
                                + AppUtils.QT(SDUtil.now(SDUtil.DATE)), false);

                db.insertSQL("Nonproductivereasonmaster", columns, values);

                db.closeDB();
            } catch (Exception e) {
                Commons.printException(e);
                return "-4";
            }

            return "1";
        }
    }

    private void customProgressDialog(AlertDialog.Builder builder) {

        try {
            LayoutInflater inflater = (LayoutInflater) activityCtxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_alert_dialog,
                    activityCtxt.findViewById(R.id.layout_root));

            TextView title = layout.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            TextView messagetv = layout.findViewById(R.id.text);
            messagetv.setText(getOwnerActivity().getResources().getString(R.string.loading));

            builder.setView(layout);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
