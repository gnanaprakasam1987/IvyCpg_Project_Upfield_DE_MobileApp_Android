package com.ivy.sd.png.view;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;


/**
 * Created by santhosh.c on 22-01-2018.
 */

@SuppressLint("ValidFragment")
public class EmailDialog extends DialogFragment implements View.OnClickListener {
    private EditText edtEmail;
    private TextView txtEmail;
    private BusinessModel bmodel;
    private onSendButtonClickListnor listner;
    private String retailerEmailId;
    @SuppressLint("ValidFragment")
    public EmailDialog(String moduleName,onSendButtonClickListnor listner,String retailerEmailId) {
        super();
        this.listner=listner;
        this.retailerEmailId=retailerEmailId;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = inflater.inflate(R.layout.email_address_dialog, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        edtEmail = (EditText) view.findViewById(R.id.edt_email);
        txtEmail=(TextView)view.findViewById(R.id.txt_email);
        edtEmail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        txtEmail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        if(!TextUtils.isEmpty(retailerEmailId))
            txtEmail.setText(retailerEmailId);
        else
            txtEmail.setText(""+getResources().getString(R.string.no_email_id));

        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);
        //edtEmail.setWidth(outMetrics.widthPixels);

        Button send = (Button) view.findViewById(R.id.btn_send);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        cancel.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        send.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        send.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_send) {

            //bmodel.setOrderHeaderNote(edtEmail.getText().toString());
            if(isValidEmail(edtEmail.getText().toString())) {
                listner.setEmailAddress(edtEmail.getText().toString());
                dismiss();
            }
            else
                Toast.makeText(bmodel, "Email is not valid", Toast.LENGTH_SHORT).show();


        } else if (i == R.id.btn_cancel) {
            listner.onCancelDialog();
            dismiss();
        }
    }
    public final static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public interface onSendButtonClickListnor
    {
        void setEmailAddress(String value);
        void onCancelDialog();
    }

}
