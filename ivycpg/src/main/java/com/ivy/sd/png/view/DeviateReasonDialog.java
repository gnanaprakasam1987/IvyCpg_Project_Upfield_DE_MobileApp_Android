package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.sd.png.asean.view.R;

/**
 * Created by rajesh.k on 3/21/2017.
 */

public class DeviateReasonDialog extends DialogFragment {
    @Nullable
    private ToggleButtonGroupTableLayout mToggleButtonGroupLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deviate_reason_dialog, container,
                false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mToggleButtonGroupLayout=(ToggleButtonGroupTableLayout)getView().findViewById(R.id.radGroup1);


    }
}
