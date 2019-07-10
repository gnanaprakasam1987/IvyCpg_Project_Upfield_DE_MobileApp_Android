package com.ivy.cpg.view.jointcall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

public class JointCallFragmentDialog extends DialogFragment {

    private BusinessModel bmodel;
    private JoinDialogInterface mJoinDialogInterface;
    private EditText mPasswordET, mRemarksET;
    private UserMasterBO mJoinUserBo;
    private String remarksLabels;

    public JointCallFragmentDialog() {
        //no operation
    }

    @SuppressLint("ValidFragment")
    public JointCallFragmentDialog(UserMasterBO userBO, JoinDialogInterface joinDialogInterface) {
        this.mJoinUserBo = userBO;
        this.mJoinDialogInterface = joinDialogInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_joint_call, container);
        Context context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();
        getDialog().setTitle("Joint Call");
        EditText mUserNameET =  view.findViewById(R.id.edit_username);
        mUserNameET.setEnabled(false);
        mPasswordET =  view.findViewById(R.id.edit_password);
        mPasswordET.requestFocus();
        mRemarksET =  view.findViewById(R.id.edit_remarks);

        remarksLabels = context.getResources().getString(R.string.enter_remarks);

        if (!bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS) {
            mRemarksET.setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.edit_remarks).getTag()) != null) {
                    ((EditText) view.findViewById(R.id.edit_remarks))
                            .setHint(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.edit_remarks)
                                            .getTag()));
                    remarksLabels = context.getResources().getString(R.string.please_enter) + bmodel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.edit_remarks)
                                    .getTag());
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        mUserNameET.setText(mJoinUserBo.getUserName());
        Button mDoneBTN =  view.findViewById(R.id.btn_done);
        Button mCancelBTN =  view.findViewById(R.id.btn_cancel);
        mCancelBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mDoneBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPasswordET.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
                } else if (bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS && mRemarksET.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), remarksLabels, Toast.LENGTH_SHORT).show();
                } else {
                    if (isJointUserLoginValidation()) {
                        mJoinUserBo.setIsJointCall(1);
                        mJoinDialogInterface.updateJoinList();
                        if (bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS)
                            mJoinDialogInterface.insertJointCallDetails(mRemarksET.getText().toString());
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.enter_valid_password), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return view;
    }

    private boolean isJointUserLoginValidation() {
        return bmodel.synchronizationHelper.validateJointCallUser(mJoinUserBo.getUserid(), mJoinUserBo.getLoginName(), mPasswordET.getText().toString());
    }
}
