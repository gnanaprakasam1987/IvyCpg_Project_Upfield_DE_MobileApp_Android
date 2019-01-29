package com.ivy.cpg.view.Planorama;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

public class PlanoramaProductFragment extends IvyBaseFragment {


    private View view;
    private ListView listView;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    BusinessModel bModel;
    PlanoramaHelper planoramaHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planorama_product, container, false);

        bModel=(BusinessModel)getActivity().getApplicationContext();
        bModel.setContext(getActivity());

        listView=view.findViewById(R.id.list);

        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : bModel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("INVT")
                    || temp.getReasonCategory().equalsIgnoreCase("NONE"))
                spinnerAdapter.add(temp);
        }

        planoramaHelper=PlanoramaHelper.getInstance(getActivity());

        listView.setAdapter(new MyAdapter(planoramaHelper.getmProductList()));

        return view;
    }


    private class MyAdapter extends BaseAdapter {
        private final ArrayList<PlanoramaProductBO> items;

        public MyAdapter(ArrayList<PlanoramaProductBO> items) {
            super();
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_planorama_product, parent, false);

                row.setTag(holder);

                holder.textView_productName = row
                        .findViewById(R.id.tvProductNameTitle);
                holder.textView_no_facing = row
                        .findViewById(R.id.textView_facings);
                holder.spinner_reason = row
                        .findViewById(R.id.spinner_reason);

                holder.spinner_reason.setAdapter(spinnerAdapter);


            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.planoramaProductBO = items.get(position);

            holder.textView_productName.setText(holder.planoramaProductBO.getProductName());
            holder.textView_no_facing.setText(String.valueOf(holder.planoramaProductBO.getNumberOfFacings()));

            if(holder.planoramaProductBO.isAvailable())
                holder.spinner_reason.setEnabled(false);
            else holder.spinner_reason.setEnabled(true);



            return row;
        }
    }

    class ViewHolder {
        PlanoramaProductBO planoramaProductBO;
        TextView textView_productName,textView_no_facing;
        Spinner spinner_reason;

    }
}
