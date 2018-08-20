package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Collections;
import java.util.Vector;

public class SubDSelectionDialog extends DialogFragment {

    private BusinessModel bmodel;
    View v;
    private ListView mCountLV;
    private TextView mTitleTV;
    Context context;
    SubIdSelectionListner subIdSelectionListner;
    int mSelectedSubId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        v = inflater.inflate(R.layout.subdselect_dialog_fragment, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mSelectedSubId = getArguments().getInt("subDId");
        Commons.print("mSelectedSubId, " + "" + mSelectedSubId);

        mTitleTV = v.findViewById(R.id.title);
        mCountLV = v.findViewById(R.id.lvSubd);

        mTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        Collections.sort(bmodel.getSubDMaster(), RetailerMasterBO.RetailerIsTodayComparator);
        mCountLV.setAdapter(new MyAdapter(bmodel.getSubDMaster()));
        mCountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (mSelectedSubId != -1) {
            int mSelectedPostion = -1;
            for (int position = 0; position < bmodel.getSubDMaster().size(); position++) {
                if (mSelectedSubId == bmodel.getSubDMaster().get(position).getSubdId())
                    mSelectedPostion = position;
            }
            if (mSelectedPostion != -1)
                mCountLV.setItemChecked(mSelectedPostion, true);
        }


        mCountLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subIdSelectionListner.onSubIdSelected(bmodel.getSubDMaster().get(position).getSubdId());
                dismiss();
            }
        });

        return v;
    }

    class ViewHolder {
        CheckedTextView tvRetailerName;
        RetailerMasterBO retaierBo;
    }

    private class MyAdapter extends ArrayAdapter<RetailerMasterBO> {
        private Vector<RetailerMasterBO> items;

        public MyAdapter(Vector<RetailerMasterBO> items) {
            super(getActivity(), android.R.layout.simple_list_item_single_choice);
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());

                convertView = inflater.inflate(R.layout.btn_radio, null);

                holder.tvRetailerName = convertView.findViewById(android.R.id.text1);
                holder.tvRetailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.retaierBo = items.get(position);
            holder.tvRetailerName.setText(holder.retaierBo.getRetailerName());
            if (holder.retaierBo.getIsToday() == 1)
                holder.tvRetailerName.setTextColor(getResources().getColor(R.color.colorAccent));
            else
                holder.tvRetailerName.setTextColor(getResources().getColor(R.color.colorPrimaryRed));

            return convertView;
        }
    }


    public boolean isShowing() {
        if (getDialog() != null) {
            return true;
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }
        int dialogHeight = (int) getActivity().getResources().getDimension(R.dimen.dialog_height); // specify a value here

        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, dialogHeight);

    }

    public interface SubIdSelectionListner {
        void onSubIdSelected(int SubID);
    }

    public void setSubIdSelectionInterface(VisitFragment visitFragment) {
        this.subIdSelectionListner = visitFragment;
    }
}
