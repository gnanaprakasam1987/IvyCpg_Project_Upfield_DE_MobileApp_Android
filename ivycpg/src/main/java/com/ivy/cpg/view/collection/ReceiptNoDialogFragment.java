package com.ivy.cpg.view.collection;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;

public class ReceiptNoDialogFragment extends DialogFragment {

    private View rootView;

    private UpdateReceiptNoInterface updateReceiptNoInterface;

    public interface UpdateReceiptNoInterface {
        void updateReceiptNo(String receiptno);
    }

    private EditText mReceiptNoET;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            updateReceiptNoInterface = (UpdateReceiptNoInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnAddFriendListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.receipno_fragment, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle("Receipt No ");
        mReceiptNoET = rootView.findViewById(R.id.et_receipno);
        Button mOkBtn = rootView.findViewById(R.id.btn_ok);

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = mReceiptNoET.getText().toString();
                if ("".equals(value)) {
                    Toast.makeText(getActivity(), "Please Enter Receiptno", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateReceiptNoInterface.updateReceiptNo(value);
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTargetFragment() != null) {
            Intent i = new Intent();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
        }
    }
}
