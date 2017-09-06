package com.ivy.sd.png.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonFieldBO;
import com.ivy.sd.png.bo.NonFieldTwoBo;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * Created by karthikeyan.a on 3/17/2016.
 */
public class NonFeildTwoDialog extends DialogFragment implements View.OnClickListener {

    private BusinessModel bmodel;
    private TextView fromDate;
    private TextView fromTime;
    private Spinner reasonSpinner;
    private ArrayAdapter<NonFieldBO> spinnerAdapter;
    private Button addButton , cancelButton;
    int reasonId = 0;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bmodel = (BusinessModel) activity.getApplicationContext();
        bmodel.setContext(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_nonfield_two, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        fromDate = (TextView) getView().findViewById(R.id.fromDate);
        fromDate.setText(SDUtil.now(SDUtil.DATE_GLOBAL));

        fromTime = (TextView) getView().findViewById(R.id.fromTime);
        fromTime.setText(SDUtil.now(SDUtil.TIME));

        reasonSpinner = (Spinner) getView().findViewById(R.id.reason);

        addButton = (Button) getView().findViewById(R.id.btn_add);
        cancelButton = (Button) getView().findViewById(R.id.bt_Cancel);

    }

    @Override
    public void onStart() {
        super.onStart();

        spinnerAdapter = new ArrayAdapter<NonFieldBO>(getActivity(),
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bmodel.mAttendanceHelper.downNonFieldReasons();
        for (NonFieldBO nonField : bmodel.mAttendanceHelper
                .getNonFieldReasonList()) {
            Commons.print("sdfsa" + nonField.getReason());
            if (nonField.getpLevelId() == 0)
                spinnerAdapter.add(nonField);
        }

        reasonSpinner.setAdapter(spinnerAdapter);

        reasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                NonFieldBO reNonFieldTwoBo = (NonFieldBO) parent.getSelectedItem();
                reasonId = reNonFieldTwoBo.getReasonID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

            if(id == R.id.btn_add){

                if (reasonId == 0) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.select_reason),
                            Toast.LENGTH_LONG).show();
                } else {

                    NonFieldTwoBo nonFieldTwoBo = new NonFieldTwoBo();
                    try {
                        nonFieldTwoBo.setFromDate(fromDate.getText().toString());
                        nonFieldTwoBo.setOutTime(fromTime.getText().toString());
                        nonFieldTwoBo.setReasonId(reasonId);
                    } catch (Exception e) {

                    }

                    bmodel.mAttendanceHelper.addNonFieldTwoWorkDetails(nonFieldTwoBo);

                    Toast.makeText(getActivity(), "Data added successfully", Toast.LENGTH_SHORT).show();

                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_OK, getActivity().getIntent());
                    dismiss();
                }
            } else if(id == R.id.bt_Cancel) {
                dismiss();
            }

    }
}
