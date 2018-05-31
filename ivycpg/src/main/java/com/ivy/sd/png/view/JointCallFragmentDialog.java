package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import com.ivy.sd.png.model.JoinDialogInterface;

public class JointCallFragmentDialog extends DialogFragment {

    private BusinessModel bmodel;
    private JoinDialogInterface mJoinDialogInterface;
    private EditText mPasswordET,mRemarksET;
    private UserMasterBO mJoinUserBo;

    public JointCallFragmentDialog(){
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
        EditText mUserNameET = (EditText) view.findViewById(R.id.edit_username);
        mUserNameET.setEnabled(false);
        mPasswordET = (EditText) view.findViewById(R.id.edit_password);
        mPasswordET.requestFocus();
        mRemarksET = (EditText) view.findViewById(R.id.edit_remarks);

        if(!bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS)
            mRemarksET.setVisibility(View.GONE);

        mUserNameET.setText(mJoinUserBo.getUserName());
        Button mDoneBTN = (Button) view.findViewById(R.id.btn_done);
        Button mCancelBTN = (Button) view.findViewById(R.id.btn_cancel);
        mCancelBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        mDoneBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mPasswordET.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Please enter password", Toast.LENGTH_SHORT).show();
                } else if(bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS && mRemarksET.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Please enter remarks", Toast.LENGTH_SHORT).show();
                }else {
                    if (isJointUserLoginValidation()) {
                        mJoinUserBo.setIsJointCall(1);
                        mJoinDialogInterface.updateJoinList();
                        if(bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS)
                            mJoinDialogInterface.insertJointCallDetails(mRemarksET.getText().toString());
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(),  getResources().getString(R.string.enter_valid_password), Toast.LENGTH_SHORT).show();
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
