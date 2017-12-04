package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

/**
 * Created by ivyuser on 4/12/17.
 */

public class PlanDeviationFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    RecyclerView reason_recycler;
    Button saveBtn;
    LinearLayout remarkLty;
    private ReasonMaster temp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_plandeviation, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        reason_recycler = (RecyclerView) rootView.findViewById(R.id.reason_recycler);
        saveBtn = (Button) rootView.findViewById(R.id.add_reason);

        reason_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (bmodel.reasonHelper.getReasonPlaneDeviationMaster() != null)
            reason_recycler.setAdapter(new DeviateReasonAdapter(bmodel.reasonHelper.getReasonPlaneDeviationMaster()));
        else
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                try {
                    temp = selected_reason;
                    NonproductivereasonBO nonproductive = new NonproductivereasonBO();
                    nonproductive.setReasonid(temp.getReasonID());
                    nonproductive.setReasontype("Field_Plan_Type");
                    nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate());
                    bmodel.savePlaneDiveateReason(nonproductive, remarks);
                } catch (Exception e) {

                }
            }
        });

        return rootView;
    }

    private ReasonMaster selected_reason;
    private String remarks = "";

    class DeviateReasonAdapter extends RecyclerView.Adapter<DeviateReasonAdapter.ViewHolder> {

        private ArrayList<ReasonMaster> items;
        private int lastCheckedPosition = -1;

        public DeviateReasonAdapter(ArrayList<ReasonMaster> items) {
            this.items = items;
        }


        @Override
        public DeviateReasonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.common_reason_popup_recycler_items, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final DeviateReasonAdapter.ViewHolder holder, final int position) {
            holder.reasonObj = items.get(position);
            holder.reason_radio_btn.setText(holder.reasonObj.getReasonDesc());

            holder.reason_radio_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastCheckedPosition = getItemViewType(position);
                    selected_reason = holder.reasonObj;
                    notifyDataSetChanged();
                }
            });
            holder.reason_radio_btn.setChecked(position == lastCheckedPosition);

            if (holder.reason_radio_btn.isChecked() && holder.reason_radio_btn.getText().toString().equals("Others")) {
                holder.edt_other_remarks.setVisibility(View.VISIBLE);
            } else {
                hideKeyboard();
                remarks = "";
                holder.edt_other_remarks.setText("");
                holder.edt_other_remarks.setVisibility(View.GONE);
            }

            holder.edt_other_remarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    remarks = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            AppCompatRadioButton reason_radio_btn;
            ReasonMaster reasonObj;
            EditText edt_other_remarks;

            public ViewHolder(View itemView) {
                super(itemView);
                reason_radio_btn = (AppCompatRadioButton) itemView.findViewById(R.id.reason_radio_btn);
                reason_radio_btn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                edt_other_remarks = (EditText) itemView.findViewById(R.id.edt_other_remarks);
            }
        }
    }


    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reason_recycler.getAdapter() != null)
            reason_recycler.setAdapter(null);
    }
}
