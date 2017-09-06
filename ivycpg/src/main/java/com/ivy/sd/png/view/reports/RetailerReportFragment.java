package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Collections;

public class RetailerReportFragment extends Fragment {

    private ListView lvwplist;
    private BusinessModel bmodel;
    private ArrayList<RetailerMasterBO> mylist;
    private View view;

    private static final int CODE_PRODUCTIVE = 1;
    private static final int CODE_NON_PRODUCTIVE = 2;
    private static final int CODE_VISITED = 3;
    private static final int CODE_QDVP3 = 4;
    private static final int CODE_INDICATIVE = 5;
    private static final int CODE_GOLDEN_STORE = 6;
    private static final int CODE_DEAD_STORE = 7;
    private static final int CODE_HANGING = 8;
    private int retailer_type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        retailer_type = getArguments().getInt("type_retailer");
        Commons.print("retailer_type" + retailer_type);
        view = inflater.inflate(R.layout.fragment_retailer_report, container,
                false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        lvwplist = (ListView) view.findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);

        getRetailerList();


        return view;

    }

    public void getRetailerList() {
        int siz = bmodel.getRetailerMaster().size();
        mylist = new ArrayList<RetailerMasterBO>();

        for (int i = 0; i < siz; i++) {
            if (retailer_type == CODE_DEAD_STORE && bmodel.getRetailerMaster().get(i).getIsDeadStore().equals("Y")) {
                mylist.add(bmodel.getRetailerMaster().get(i));
            } else if (retailer_type == CODE_GOLDEN_STORE && bmodel.getRetailerMaster().get(i).getIsGoldStore() == 1) {
                mylist.add(bmodel.getRetailerMaster().get(i));
            } else if (retailer_type == CODE_HANGING && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                mylist.add(bmodel.getRetailerMaster().get(i));
            } else if (retailer_type == CODE_INDICATIVE && bmodel.getRetailerMaster().get(i).getIndicateFlag() == 1) {
                mylist.add(bmodel.getRetailerMaster().get(i));
            } else if (retailer_type == CODE_NON_PRODUCTIVE) {
                if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                        && bmodel.getRetailerMaster().get(i).isInvoiceDone().equals("Y")) {
                    mylist.add(bmodel.getRetailerMaster().get(i));
                } else if (!bmodel.configurationMasterHelper.IS_INVOICE && bmodel.getRetailerMaster().get(i).isOrdered().equals("Y")) {
                    mylist.add(bmodel.getRetailerMaster().get(i));
                }

            } else if (retailer_type == CODE_PRODUCTIVE) {
                if (bmodel.getRetailerMaster().get(i).isOrdered().equals("Y")) {
                    mylist.add(bmodel.getRetailerMaster().get(i));
                } else if (bmodel.configurationMasterHelper.IS_INVOICE && bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                        && bmodel.getRetailerMaster().get(i).isInvoiceDone().equals("Y")) {
                    mylist.add(bmodel.getRetailerMaster().get(i));
                }

            } else if (retailer_type == CODE_QDVP3 && bmodel.getRetailerMaster().get(i).getRField4().equals("1")) {
                mylist.add(bmodel.getRetailerMaster().get(i));
            } else if (retailer_type == CODE_VISITED && bmodel.getRetailerMaster().get(i).getIsVisited().equals("Y")) {
                mylist.add(bmodel.getRetailerMaster().get(i));
            }
        }
        Collections.sort(mylist, RetailerMasterBO.RetailerNameComparator);
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    private RetailerMasterBO retailer;

    private class MyAdapter extends ArrayAdapter<RetailerMasterBO> {
        private ArrayList<RetailerMasterBO> items;

        public MyAdapter(ArrayList<RetailerMasterBO> items) {
            super(getActivity(), R.layout.row_retailer_report_listview, items);
            this.items = items;
        }

        public RetailerMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            retailer = (RetailerMasterBO) items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_retailer_report_listview,
                        parent, false);
                holder = new ViewHolder();
                holder.retname = (TextView) row.findViewById(R.id.retailername);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.retname.setText(retailer.getRetailerName());

            return row;
        }
    }

    class ViewHolder {
        TextView retname;
    }


}
