package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

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
    private AlertDialog alertDialog;
    private ArrayList<NonproductivereasonBO> reasonList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_plandeviation, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        //Set Screen Title
        try {
            if (getArguments().getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_NON_FIELD"));
            else
                setScreenTitle(getArguments().getString("screentitle"));
        } catch (Exception e) {
        }

        reason_recycler = (RecyclerView) rootView.findViewById(R.id.reason_recycler);
        saveBtn = (Button) rootView.findViewById(R.id.add_reason);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

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
                    if (reasonList.size() > 0)
                        new SavePlaneDeviateReason().execute();
                    else
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.no_data_tosave),
                                Toast.LENGTH_SHORT).show();

                } catch (Exception e) {

                }
            }
        });

        return rootView;
    }


    // Save the Plane deviate reason Work Details
    private class SavePlaneDeviateReason extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.savePlaneDiveateReason(reasonList, remarks);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(i);
            getActivity().finish();

        }
    }


    private String remarks = "";

    class DeviateReasonAdapter extends RecyclerView.Adapter<DeviateReasonAdapter.ViewHolder> {

        private ArrayList<ReasonMaster> items;

        public DeviateReasonAdapter(ArrayList<ReasonMaster> items) {
            this.items = items;
        }


        @Override
        public DeviateReasonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.deivation_reason_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final DeviateReasonAdapter.ViewHolder holder, final int position) {
            holder.reasonObj = items.get(position);
            holder.cbReason.setText(holder.reasonObj.getReasonDesc());

            holder.cbReason.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        NonproductivereasonBO nonproductive = new NonproductivereasonBO();
                        nonproductive.setReasonid(holder.reasonObj.getReasonID());
                        nonproductive.setReasontype("Field_Plan_Type");
                        nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO()
                                .getDownloadDate());
                        reasonList.add(nonproductive);
                    } else {
                        for (int i = 0; i < reasonList.size(); i++) {
                            if (reasonList.get(i).getReasonid().equals(holder.reasonObj.getReasonID())) {
                                reasonList.remove(i);
                                break;
                            }
                        }
                    }
                    notifyDataSetChanged();
                }
            });

            if (holder.cbReason.isChecked() && holder.cbReason.getText().toString().equals("Others")) {
                holder.edt_other_remarks.setVisibility(View.VISIBLE);
            }  else {
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
            CheckBox cbReason;
            ReasonMaster reasonObj;
            EditText edt_other_remarks;

            public ViewHolder(View itemView) {
                super(itemView);
                cbReason = (CheckBox) itemView.findViewById(R.id.cb_reason);
                cbReason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
