package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;


/**
 * Created by mansoor.k on 30-01-2018.
 * Dialog class Allow to switch user and re download data
 */

@SuppressLint("ValidFragment")
public class SwitchUserDialog extends DialogFragment implements OnClickListener {
    private onSwitchUser listner;
    private BusinessModel bmodel;
    private EditText edtUserNanme, edtPassword;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listner = (onSwitchUser) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement onSwitchUser");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        View view = inflater.inflate(R.layout.switch_user_dialog, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        edtUserNanme = (EditText) view.findViewById(R.id.username);
        edtPassword = (EditText) view.findViewById(R.id.password);
        edtUserNanme.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edtPassword.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);

        Button switchUser = (Button) view.findViewById(R.id.btn_switchuser);
        switchUser.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        switchUser.setOnClickListener(this);

        Button btnClose = (Button) view.findViewById(R.id.btnCancel);
        btnClose.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnClose.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_switchuser) {

            if (edtUserNanme.getText().toString().equals("")) {
                edtUserNanme.requestFocus();
                edtUserNanme.setError(getResources().getString(R.string.enter_username));
                return;
            } else if (edtPassword.getText().toString().equals("")) {
                edtPassword.requestFocus();
                edtPassword.setError(getResources().getString(R.string.enter_password));
                return;
            }

            listner.setUserNamePwd(edtUserNanme.getText().toString(), edtPassword.getText().toString());
            dismiss();

        }

        if (i == R.id.btnCancel) {
            dismiss();
        }
    }

    public interface onSwitchUser {
        void setUserNamePwd(String userName, String password);
    }
}
