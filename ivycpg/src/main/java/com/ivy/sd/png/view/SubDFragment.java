package com.ivy.sd.png.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Vector;


public class SubDFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    ListView lvSubDId;
    Context context;
    Vector<RetailerMasterBO> retailer = new Vector<>();
    RetailerSelectionAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sub_d, container, false);

        context = getActivity();
        retailer = bmodel.getSubDMaster();
        lvSubDId = (ListView) view.findViewById(R.id.lv_subdid);

        lvSubDId.setDivider(null);
        lvSubDId.setDividerHeight(0);
        adapter = new RetailerSelectionAdapter(retailer);
        lvSubDId.setAdapter(adapter);
        return view;
    }


    private class RetailerSelectionAdapter extends ArrayAdapter<RetailerMasterBO> {

        private final Vector<RetailerMasterBO> items;
        LayoutInflater inflater;

        private RetailerSelectionAdapter(Vector<RetailerMasterBO> items) {
            super(context, R.layout.visit_list_child_item, items);
            this.items = items;
            inflater = LayoutInflater.from(context);
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

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final RetailerSelectionAdapter.ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_subdid_layout, parent, false);
                holder = new RetailerSelectionAdapter.ViewHolder();

                holder.retailertNameTextView = (TextView) convertView.findViewById(R.id.retailer_name_subdid);

                holder.retailertNameTextView.setTypeface(bmodel.configurationMasterHelper
                        .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                convertView.setTag(holder);
            } else {
                holder = (RetailerSelectionAdapter.ViewHolder) convertView.getTag();
            }

            String tvText = items.get(position).getRetailerName();
            holder.retailertNameTextView.setText(tvText);
            return convertView;
        }

        class ViewHolder {
            private TextView retailertNameTextView;

        }
    }

}
