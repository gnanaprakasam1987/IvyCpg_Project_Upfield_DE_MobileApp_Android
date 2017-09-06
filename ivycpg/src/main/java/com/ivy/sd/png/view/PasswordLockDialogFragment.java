package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;

/**
 * Created by rajesh.k on 12/16/2016.
 */

public class PasswordLockDialogFragment extends DialogFragment {
    @Nullable

    private String mTitle = "";
    private String mTextViewTitle="";
    private View rootView;
    private TextView mTitleTV;
    private Button mOkBtn, mDismisBtn;
    private ListView mCountLV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getArguments().getString("title");
        mTextViewTitle=getArguments().getString("textviewTitle");
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(mTitle);
        getDialog().setCancelable(false);
        mTitleTV = (TextView) rootView.findViewById(R.id.title);
        mOkBtn = (Button) rootView.findViewById(R.id.btn_ok);
        mDismisBtn = (Button) rootView.findViewById(R.id.btn_dismiss);
        mTitleTV.setText(mTextViewTitle);
        mCountLV = (ListView) rootView.findViewById(R.id.lv_colletion_print);
        mCountLV.setVisibility(View.GONE);
        mOkBtn.setVisibility(View.GONE);

        mDismisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
