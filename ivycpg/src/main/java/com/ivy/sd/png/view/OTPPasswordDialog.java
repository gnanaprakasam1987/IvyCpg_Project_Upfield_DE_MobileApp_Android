package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
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

public class OTPPasswordDialog extends Dialog implements OnClickListener {
    private Button ok, cancel;
    private EditText password;
    private BusinessModel bmodel;
    //private ProgressDialog pd;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private OnDismissListener otpPasswordDismissListener;
    private RetailerMasterBO mRetailerBO;
    private FragmentActivity activityCtxt;
    private TextView messageTv, titleBar, contactnoTxtView;
    private int flag = 0;
    private Spinner reason;
    private ArrayAdapter<SpinnerBO> reasonAdapter;

    public OTPPasswordDialog(FragmentActivity activity, BusinessModel bmodel,
                             OnDismissListener otpPasswordDismissListener,
                             RetailerMasterBO retailerBO, String strTitle, int flag) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.otp_password_dialog);
        titleBar = (TextView) findViewById(R.id.titleBar);
        titleBar.setText(strTitle);
        this.otpPasswordDismissListener = otpPasswordDismissListener;
        activityCtxt = activity;
        this.bmodel = bmodel;
        this.flag = flag;
        mRetailerBO = retailerBO;
        password = (EditText) findViewById(R.id.passwordEditText);
        messageTv = (TextView) findViewById(R.id.messageTv);
        reason = (Spinner) findViewById(R.id.reason);
        contactnoTxtView = (TextView) findViewById(R.id.contactnoTxtView);
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

        builder = new AlertDialog.Builder(activityCtxt);

        if ((flag == 2) && bmodel.configurationMasterHelper.ret_skip_flag == 2) {
            reason.setVisibility(View.VISIBLE);
            reasonAdapter = new ArrayAdapter<SpinnerBO>(activityCtxt,
                    R.layout.spinner_bluetext_layout);
            reasonAdapter.add(new SpinnerBO(0, activity.getResources()
                    .getString(R.string.select_reason)));
            loadOTPReason();
            reasonAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            reason.setAdapter(reasonAdapter);
            //  pd.setTitle(activityCtxt.getResources().getString(
            //   R.string.saving));

        } else {
            password.setVisibility(View.VISIBLE);
            //  pd.setTitle(activityCtxt.getResources().getString(
            //   R.string.checking_password));
        }
        password.setRawInputType(Configuration.KEYBOARD_12KEY);
        ok = (Button) findViewById(R.id.btn_ok);
        cancel = (Button) findViewById(R.id.btn_cancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

		/*pd.setMessage(activityCtxt.getResources().getString(
                R.string.loading_data));
		
		pd.setIndeterminate(true);
		pd.setCancelable(false);*/

        bmodel.customProgressDialog(alertDialog, builder, activityCtxt, activityCtxt.getResources().getString(R.string.loading));
        alertDialog = builder.create();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_ok) {
            if ((flag == 2) && bmodel.configurationMasterHelper.ret_skip_flag == 2)
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
            DBUtil db = new DBUtil(activityCtxt, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            if (result.equals("1")) {
                if (flag == 1)
                    mRetailerBO.setOtpActivatedDate(SDUtil
                            .now(SDUtil.DATE_GLOBAL));
                else if (bmodel.configurationMasterHelper.ret_skip_flag == 2
                        || flag == 2) {
                    mRetailerBO.setSkipActivatedDate(SDUtil
                            .now(SDUtil.DATE_GLOBAL));
                    mRetailerBO.setSkip(true);
                }
                bmodel.saveOTPActivatedDate(mRetailerBO.getRetailerID(), flag);
                doDismiss();
            } else if (result.equals("-1")) {
                messageTv.setText(activityCtxt.getResources().getString(
                        R.string.otp_expired));
            } else if (result.equals("-2")) {
                messageTv.setText(activityCtxt.getResources().getString(
                        R.string.invalid_otp));
            } else if (result.equals("-3")) {
                messageTv.setText(activityCtxt.getResources().getString(
                        R.string.activated_already));
            } else if (result.equals("-4")) {
                messageTv.setText(activityCtxt.getResources().getString(
                        R.string.activation_error_try_again));
            } else if (result.equals("-5")) {
                messageTv.setText("Session Expired ");
            }
            doCancel();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                if (bmodel.configurationMasterHelper.ret_skip_flag == 2)
                    return saveReason();
                else if (flag == 1) // For Retailer GPS OTP
                    return bmodel
                            .checkOTP(
                                    mRetailerBO.getRetailerID(),
                                    password.getText().toString(),
                                    bmodel.getStandardListIdAndType(
                                            StandardListMasterConstants.SLM_RET_GPS_CODE,
                                            StandardListMasterConstants.OTP_LIST_CODE));
                else if (flag == 2) // For Retailer Skip OTP
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
                DBUtil db = new DBUtil(activityCtxt, DataMembers.DB_NAME,
                        DataMembers.DB_PATH);
                String values;
                db.createDataBase();
                db.openDataBase();

                String id;

                String columns = "UID,RetailerID,RouteID,Date,ReasonID,ReasonTypes,upload,DistributorID";

                id = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorid()
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "" + SDUtil.now(SDUtil.DATE_TIME_ID));

                values = id
                        + ","
                        + bmodel.QT(mRetailerBO.getRetailerID())
                        + ","
                        + mRetailerBO.getBeatID()
                        + ","
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + ","
                        + ((SpinnerBO) reason.getSelectedItem()).getId()
                        + ","
                        + bmodel.QT(bmodel
                        .getStandardListId(StandardListMasterConstants.OTP_REASON_TYPE))
                        + "," + bmodel.QT("N")
                        + "," + mRetailerBO.getDistributorId();

                db.deleteSQL(
                        "Nonproductivereasonmaster",
                        "RetailerID="
                                + bmodel.QT(mRetailerBO.getRetailerID())
                                + " and DistributorId="
                                + mRetailerBO.getDistributorId()
                                + " and ReasonTypes="
                                + bmodel.QT(bmodel
                                .getStandardListId(StandardListMasterConstants.OTP_REASON_TYPE))
                                + " and Date="
                                + bmodel.QT(SDUtil.now(SDUtil.DATE)), false);

                db.insertSQL("Nonproductivereasonmaster", columns, values);

                db.closeDB();
            } catch (Exception e) {
                Commons.printException(e);
                return "-4";
            }

            return "1";
        }
    }


}
