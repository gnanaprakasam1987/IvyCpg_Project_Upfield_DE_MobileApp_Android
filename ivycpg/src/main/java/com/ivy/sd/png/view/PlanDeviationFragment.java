package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.homescreen.HomeScreenFragment;
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
    RecyclerView reason_recycler, planned_recycler;
    Button saveBtn;
    private AlertDialog alertDialog;
    private ArrayList<NonproductivereasonBO> reasonList = new ArrayList<>();
    private ArrayAdapter<ReasonMaster> reasonAdapter;


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
        planned_recycler = (RecyclerView) rootView.findViewById(R.id.planned_recycler);
        reason_recycler = (RecyclerView) rootView.findViewById(R.id.reason_recycler);
        saveBtn = (Button) rootView.findViewById(R.id.add_reason);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        planned_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        reason_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (bmodel.reasonHelper.getReasonPlaneDeviationMaster() != null)
            reason_recycler.setAdapter(new DeviateReasonAdapter(bmodel.reasonHelper.getReasonPlaneDeviationMaster()));
        else
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();


        if (bmodel.reasonHelper.getReasonPlanedActivities() != null)
            planned_recycler.setAdapter(new PlannedReasonAdapter(bmodel.reasonHelper.getReasonPlanedActivities()));


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                try {
                    if (reasonList.size() > 0) {
                        if (checkDeviatedActivity(reasonList))
                            new SavePlaneDeviateReason().execute();
                        else
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.select_reason),
                                    Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.no_data_tosave),
                                Toast.LENGTH_SHORT).show();

                } catch (Exception e) {

                }
            }
        });

        reasonAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        reasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        ReasonMaster reason = new ReasonMaster();
        reason.setReasonID("0");
        reason.setReasonDesc(getResources().getString(R.string.select_reason));
        reason.setReasonCategory("");
        reasonAdapter.add(reason);

        for (ReasonMaster deviationReasobn : bmodel.reasonHelper.getNonPlanedReasons()) {
            reasonAdapter.add(deviationReasobn);
        }

        return rootView;
    }

    private boolean checkDeviatedActivity(ArrayList<NonproductivereasonBO> reasonList) {

        for (int index = 0; index < reasonList.size(); index++) {
            if (reasonList.get(index).getIsPlanned() == 0 && reasonList.get(index).getDeviationReason().equalsIgnoreCase(getResources().getString(R.string.select_reason))) {
                return false;
            }
        }

        return true;
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

            if (!alertDialog.isShowing())
                onCreateDialogNew();


        }
    }


    protected void onCreateDialogNew() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(null)
                .setTitle(getResources().getString(R.string.saved_successfully))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                HomeScreenFragment currentFragment = (HomeScreenFragment) ((FragmentActivity) getActivity()).getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
                                if (currentFragment != null) {
                                    currentFragment.detach("MENU_NON_FIELD");
                                }
                            }
                        });
        bmodel.applyAlertDialogTheme(builder);
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
            holder.deviationReason.setAdapter(reasonAdapter);

            holder.cbReason.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        NonproductivereasonBO nonproductive = new NonproductivereasonBO();
                        nonproductive.setReasonid(holder.reasonObj.getReasonID());
                        nonproductive.setReasontype("Field_Plan_Type");
                        nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO()
                                .getDownloadDate());
                        nonproductive.setIsPlanned(holder.reasonObj.getIsPlanned());
                        nonproductive.setDeviatedReasonId("0");
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

            holder.deviationReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ReasonMaster reasonMaster = (ReasonMaster) holder.deviationReason
                            .getSelectedItem();

                    for (int index = 0; index < reasonList.size(); index++) {
                        if (reasonList.get(index).getReasonid().equals(holder.reasonObj.getReasonID())) {
                            reasonList.get(index).setDeviationReason(reasonMaster.getReasonDesc());
                            reasonList.get(index).setDeviatedReasonId(reasonMaster.getReasonID());
                            break;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if (reasonList.size() > 0) {

                int pos = 0;
                for (int index = 0; index < reasonList.size(); index++) {
                    if (reasonList.get(index).getReasonid() == holder.reasonObj.getReasonID()) {
                        pos = getPosition(Integer.parseInt(reasonList.get(index).getDeviatedReasonId()));
                    } else {
                        continue;
                    }

                }

                holder.deviationReason.setSelection(pos);

            } else {
                holder.deviationReason.setSelection(0);
            }

            if (holder.cbReason.isChecked() && holder.reasonObj.getIsPlanned() == 0)
                holder.reasonLayout.setVisibility(View.VISIBLE);
            else
                holder.reasonLayout.setVisibility(View.GONE);

            if (holder.cbReason.isChecked() && holder.cbReason.getText().toString().equals("Others")) {
                holder.edt_other_remarks.setVisibility(View.VISIBLE);
                holder.edt_other_remarks.setCursorVisible(true);
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
            CheckBox cbReason;
            ReasonMaster reasonObj;
            EditText edt_other_remarks;
            Spinner deviationReason;
            LinearLayout reasonLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                cbReason = (CheckBox) itemView.findViewById(R.id.cb_reason);
                cbReason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                edt_other_remarks = (EditText) itemView.findViewById(R.id.edt_other_remarks);
                deviationReason = (Spinner) itemView.findViewById(R.id.reasonSpinner);
                reasonLayout = (LinearLayout) itemView.findViewById(R.id.reasonLayout);
            }
        }
    }


    private int getPosition(int reasonId) {

        int position = 0;

        for (int index = 0; index < bmodel.reasonHelper.getNonPlanedReasons().size(); index++) {

            if (reasonId == Integer.parseInt(bmodel.reasonHelper.getNonPlanedReasons().get(index).getReasonID())) {
                position = index + 1;
                break;
            }
        }

        return position;
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
        if (planned_recycler.getAdapter() != null)
            planned_recycler.setAdapter(null);
    }


    class PlannedReasonAdapter extends RecyclerView.Adapter<PlannedReasonAdapter.ViewHolder> {

        private ArrayList<ReasonMaster> items;

        public PlannedReasonAdapter(ArrayList<ReasonMaster> items) {
            this.items = items;
        }


        @Override
        public PlannedReasonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.planned_nonfield_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final PlannedReasonAdapter.ViewHolder holder, final int position) {
            holder.reasonObj = items.get(position);
            holder.plannedTextView.setText(holder.reasonObj.getReasonDesc());

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
            TextView plannedTextView;
            ReasonMaster reasonObj;


            public ViewHolder(View itemView) {
                super(itemView);
                plannedTextView = (TextView) itemView.findViewById(R.id.plannedActivityTextView);
                plannedTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }
    }
}
