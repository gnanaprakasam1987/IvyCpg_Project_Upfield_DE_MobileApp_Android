package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PlanningOutletBO;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nivetha.s on 26-10-2015.
 */
public class PlanningOutletFragment extends Fragment {
    View view;
    private MyAdapterForPlanningOutletProducts adapter;
    private BusinessModel bmodel;
    private ListView planningOutlerListview, productsListView;
    private ArrayList<SKUWiseTargetBO> list;
    private ArrayList<PlanningOutletBO> skulist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        view = inflater.inflate(R.layout.day_planning_outlet, container,
                false);
        planningOutlerListview = (ListView) view.findViewById(R.id.planninglv);
        planningOutlerListview.setCacheColorHint(0);
        productsListView = (ListView) view.findViewById(R.id.planninglv1);
        bmodel.dashBoardHelper.findMinMaxProductLevel(bmodel
                .getRetailerMasterBO().getRetailerID());

        bmodel.dashBoardHelper.downloadSKUWiseTarget(bmodel
                .getRetailerMasterBO().getRetailerID(), "MONTH", "");
        bmodel.dashBoardHelper.downloadDashboardLevelSkip(1);
        list = bmodel.dashBoardHelper.getSkuWiseTarget();
        MyAdapterForPlanningOutlet adapter = new MyAdapterForPlanningOutlet(
                bmodel.profilehelper.downloadPlanningOutletCategory());
        planningOutlerListview.setAdapter(adapter);
        return view;
    }

    PlanningOutletBO pbo;

    class MyAdapterForPlanningOutlet extends ArrayAdapter {

        private ArrayList<PlanningOutletBO> items;

        public MyAdapterForPlanningOutlet(ArrayList<PlanningOutletBO> items) {
            super(getActivity(), R.layout.row_planning_outlet, items);
            this.items = items;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final Holder holder;
            pbo = (PlanningOutletBO) items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_planning_outlet, parent,
                        false);

                holder = new Holder();
                holder.pname = (TextView) row.findViewById(R.id.pname);
                holder.tgt1 = (TextView) row.findViewById(R.id.tgt1);
                holder.tgt2 = (TextView) row.findViewById(R.id.tgt2);
                holder.tgt3 = (TextView) row.findViewById(R.id.tgt3);

                holder.ach1 = (TextView) row.findViewById(R.id.ach1);
                holder.ach2 = (TextView) row.findViewById(R.id.ach2);
                holder.ach3 = (TextView) row.findViewById(R.id.ach3);
                holder.Keybattles = (TextView) row.findViewById(R.id.keybattles);
                holder.Keybattles.setVisibility(View.GONE);
                holder.pname.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            List<Integer> ids = new ArrayList<Integer>();
                            skulist = new ArrayList<PlanningOutletBO>();
                            PlanningOutletBO pbo = (PlanningOutletBO) bmodel.profilehelper
                                    .downloadPlanningOutletCategory().get(
                                            position);
                            int pid = pbo.getPid();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getParentID() == pid)
                                    if (!ids.contains(list.get(i).getPid()))
                                        ids.add(list.get(i).getPid());
                            }
                            Commons.print("ids" + ids);
                            if (ids.size() > 0)
                                for (PlanningOutletBO bo : bmodel.profilehelper
                                        .downloadPlanningOutletBrand(ids))
                                    skulist.add(bo);
                            adapter = new MyAdapterForPlanningOutletProducts(
                                    skulist);
                            productsListView.setAdapter(adapter);

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Commons.printException(e);
                        }

                    }
                });
                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }
            holder.pbo = pbo;
            holder.pname.setText(holder.pbo.getPname());
            holder.Keybattles.setText(holder.pbo.getKeyBattles());
            for (int i = 0; i < holder.pbo.getPlanlist().size(); i++) {
                if (i == 2) {
                    holder.tgt1.setText(holder.pbo.getPlanlist().get(i)
                            .getTargetm1()
                            + "");
                    holder.ach1.setText(holder.pbo.getPlanlist().get(i)
                            .getAchievedm1()
                            + "");
                } else if (i == 1) {
                    holder.tgt2.setText(holder.pbo.getPlanlist().get(i)
                            .getTargetm1()
                            + "");
                    holder.ach2.setText(holder.pbo.getPlanlist().get(i)
                            .getAchievedm1()
                            + "");
                } else if (i == 0) {
                    holder.tgt3.setText(holder.pbo.getPlanlist().get(i)
                            .getTargetm1()
                            + "");
                    holder.ach3.setText(holder.pbo.getPlanlist().get(i)
                            .getAchievedm1()
                            + "");
                } else {
                    holder.tgt1.setText("0");
                    holder.ach1.setText("0");
                    holder.tgt2.setText("0");
                    holder.tgt3.setText("0");
                    holder.ach2.setText("0");
                    holder.ach3.setText("0");
                }
            }
            return (row);
        }
    }

    class Holder {
        TextView pname, tgt1, tgt2, tgt3, ach1, ach2, ach3, Keybattles;
        PlanningOutletBO pbo;
    }

    PlanningOutletBO skubo;

    class MyAdapterForPlanningOutletProducts extends ArrayAdapter {
        private ArrayList<PlanningOutletBO> items;

        public MyAdapterForPlanningOutletProducts(
                ArrayList<PlanningOutletBO> items) {
            super(getActivity(), R.layout.row_planning_outlet, items);
            this.items = items;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final VwHolder holder;
            skubo = (PlanningOutletBO) items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_planning_outlet, parent,
                        false);

                holder = new VwHolder();
                holder.pname = (TextView) row.findViewById(R.id.pname);
                holder.tgt1 = (TextView) row.findViewById(R.id.tgt1);
                holder.tgt2 = (TextView) row.findViewById(R.id.tgt2);
                holder.tgt3 = (TextView) row.findViewById(R.id.tgt3);

                holder.ach1 = (TextView) row.findViewById(R.id.ach1);
                holder.ach2 = (TextView) row.findViewById(R.id.ach2);
                holder.ach3 = (TextView) row.findViewById(R.id.ach3);

                holder.keybattles = (TextView) row.findViewById(R.id.keybattles);
                row.setTag(holder);
            } else {
                holder = (VwHolder) row.getTag();
            }
            holder.skubo = skubo;
            holder.pname.setText(holder.skubo.getPname());
            holder.keybattles.setText(holder.skubo.getKeyBattles());
            for (int i = 0; i < holder.skubo.getPlanlist().size(); i++) {
                if (i == 2) {
                    holder.tgt1.setText(holder.skubo.getPlanlist().get(i)
                            .getTargetm1()
                            + "");
                    holder.ach1.setText(holder.skubo.getPlanlist().get(i)
                            .getAchievedm1()
                            + "");
                } else if (i == 1) {
                    holder.tgt2.setText(holder.skubo.getPlanlist().get(i)
                            .getTargetm1()
                            + "");
                    holder.ach2.setText(holder.skubo.getPlanlist().get(i)
                            .getAchievedm1()
                            + "");
                } else if (i == 0) {
                    holder.tgt3.setText(holder.skubo.getPlanlist().get(i)
                            .getTargetm1()
                            + "");
                    holder.ach3.setText(holder.skubo.getPlanlist().get(i)
                            .getAchievedm1()
                            + "");
                } else {
                    holder.tgt1.setText("0");
                    holder.ach1.setText("0");
                    holder.tgt2.setText("0");
                    holder.tgt3.setText("0");
                    holder.ach2.setText("0");
                    holder.ach3.setText("0");
                }
            }
            return (row);
        }
    }

    class VwHolder {
        TextView pname, tgt1, tgt2, tgt3, ach1, ach2, ach3, keybattles;
        PlanningOutletBO skubo;
    }
}
