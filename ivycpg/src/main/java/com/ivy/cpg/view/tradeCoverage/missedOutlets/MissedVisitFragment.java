package com.ivy.cpg.view.tradeCoverage.missedOutlets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.ivy.cpg.view.tradeCoverage.deviation.DeviationHelper;
import com.ivy.cpg.view.tradeCoverage.deviation.PlanningActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.RetailerMissedVisitBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.MissedCallDialog;

import java.util.ArrayList;
import java.util.Vector;

public class MissedVisitFragment extends Fragment {

    MissedCallDialog dialogFragment;
    private BusinessModel bmodel;
    private ListView listView;
    private ArrayList<RetailerMissedVisitBO> mUpdateMissedRetailerList;
    private TypedArray typearr;
    private String calledBy = "";
    private RetailerMissedVisitBO mSelectedMissedRetailer;
    private DeviationHelper deviationHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View mView = inflater.inflate(R.layout.missedvisit, container, false);
        listView = mView.findViewById(R.id.missedlistView);
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        try {
            if (bmodel.labelsMasterHelper.applyLabels(mView.findViewById(
                    R.id.retnameTitle).getTag()) != null)
                ((TextView) mView.findViewById(R.id.retnameTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(mView.findViewById(
                                        R.id.retnameTitle)
                                        .getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(mView.findViewById(
                    R.id.missedTitle).getTag()) != null)
                ((TextView) mView.findViewById(R.id.missedTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(mView.findViewById(
                                        R.id.missedTitle)
                                        .getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(mView.findViewById(
                    R.id.plannedTitle).getTag()) != null)
                ((TextView) mView.findViewById(R.id.plannedTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(mView.findViewById(
                                        R.id.plannedTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return mView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        deviationHelper = new DeviationHelper(bmodel);

        calledBy = getActivity().getIntent().getStringExtra("From");
        if (calledBy == null)
            calledBy = "MENU_VISIT";
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mUpdateMissedRetailerList = new ArrayList<>();
        ArrayList<RetailerMissedVisitBO> mMissedRetailerList = bmodel.mRetailerHelper.getMissedRetailerlist();
        Vector<RetailerMasterBO> retailerMaster = bmodel.getRetailerMaster();
        if (mMissedRetailerList != null) {
            for (RetailerMissedVisitBO retailerMissedBO : mMissedRetailerList) {
                for (RetailerMasterBO retailerMasterBO : retailerMaster) {

                    if (retailerMasterBO.getRetailerID().equals(
                            retailerMissedBO.getRetailerId())
                            && retailerMasterBO.getBeatID() == retailerMissedBO
                            .getBeatId()
                            && (retailerMasterBO.getIsDeviated() != null &&  "N".equals(retailerMasterBO.getIsDeviated()))) {
                        mUpdateMissedRetailerList.add(retailerMissedBO);
                        break;
                    }

                }
            }
            MyAdapter adapter = new MyAdapter();
            ((PlanningActivity) getActivity()).updateRetailerCount(adapter.getCount(), 3);
            listView.setAdapter(adapter);

        }

    }

    public void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getResources().getString(
                R.string.no_visit_Planning));
        builder.setMessage(msg);
        final Spinner input = new Spinner(getActivity());

        ArrayAdapter<ReasonMaster> spinnerAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item,
                bmodel.reasonHelper.getDeviatedReturnMaster());

        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(spinnerAdapter);

        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        ReasonMaster r = (ReasonMaster) input.getSelectedItem();
                        Commons.print(r.getReasonDesc() + "DESC");
                        if (!r.getReasonDesc().equalsIgnoreCase(
                                getResources()
                                        .getString(R.string.select_reason))) {
                            deviationHelper.setDeviate(
                                    mSelectedMissedRetailer.getRetailerId(), r,
                                    mSelectedMissedRetailer.getBeatId(), "");

                            loadData();
                        }
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUpdateMissedRetailerList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            String tvText;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.missed_visit_list_item,
                        parent, false);
                holder = new ViewHolder();
                holder.retailerNameTV = convertView
                        .findViewById(R.id.tv_retailer_name);
                holder.missedTV = convertView
                        .findViewById(R.id.tv_missed);
                holder.plannedVisitTV = convertView
                        .findViewById(R.id.tv_planned);
                holder.missedTV.setPaintFlags(holder.missedTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSelectedMissedRetailer = holder.missedRetailerBO;

                        Commons.print("-"
                                + bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION);
                        Commons.print("-" + calledBy);

                        if ((bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION && "MENU_PLANNING".equals(calledBy))
                                || (bmodel.configurationMasterHelper.IS_VISITSCREEN_DEV_ALLOW && bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION)) {

                            if (bmodel.reasonHelper.getDeviatedReturnMaster()
                                    .size() != 0) {

                                if (!bmodel
                                        .isAlreadyExistInToday(mSelectedMissedRetailer
                                                .getRetailerId())) {
                                    showAlert(
                                            getResources()
                                                    .getString(
                                                            R.string.enter_deviate_reason_to_plan_this_retailer_for_today));
                                } else {
                                    Toast.makeText(
                                            getActivity()
                                                    .getApplicationContext(),
                                            getResources()
                                                    .getString(
                                                            R.string.retailer_is_already_planned_for_today),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {

                                Toast t = Toast
                                        .makeText(
                                                getActivity()
                                                        .getApplicationContext(),
                                                getResources()
                                                        .getString(
                                                                R.string.no_deviate_reason_found_plz_redownload),
                                                Toast.LENGTH_SHORT);
                                t.show();
                            }
                        } else {
                            Toast t = Toast.makeText(getActivity()
                                            .getApplicationContext(), getResources()
                                            .getString(R.string.Deviation_not_allowed),
                                    Toast.LENGTH_SHORT);
                            t.show();
                        }

                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.missedRetailerBO = mUpdateMissedRetailerList.get(pos);
            tvText = holder.missedRetailerBO
                    .getRetailerName() + "";
            holder.retailerNameTV.setText(tvText);

            tvText = holder.missedRetailerBO.getMissedCount()
                    + "";
            holder.missedTV.setText(tvText);

            tvText = holder.missedRetailerBO.getPlannedVisitCount()
                    + "";
            holder.plannedVisitTV.setText(tvText);

            if (pos % 2 == 0)
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            else
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));

            holder.missedTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.missedRetailerBO.getMissedCount() > 0) {
                        bmodel.mRetailerHelper.downloadMissedRetailerDetails(holder.missedRetailerBO.getRetailerId());

                        if (dialogFragment != null) {
                            if (dialogFragment.isShowing()) {
                                return;
                            }
                        }

                        dialogFragment = new MissedCallDialog();
                        dialogFragment.show(getActivity().getSupportFragmentManager(), "MissedCallDialog");
                    }
                }
            });

            return convertView;
        }

    }

    class ViewHolder {
        TextView retailerNameTV;
        TextView missedTV;
        TextView plannedVisitTV;
        RetailerMissedVisitBO missedRetailerBO;

    }


}
