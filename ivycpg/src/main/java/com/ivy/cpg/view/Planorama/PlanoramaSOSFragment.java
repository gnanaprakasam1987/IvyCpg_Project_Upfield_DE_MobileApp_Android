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
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

public class PlanoramaSOSFragment extends IvyBaseFragment {

    private View view;
    private ListView listView;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    BusinessModel bModel;
    PlanoramaHelper planoramaHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planorama_sos, container, false);

        bModel=(BusinessModel)getActivity().getApplicationContext();
        bModel.setContext(getActivity());

        listView=view.findViewById(R.id.list);


        planoramaHelper=PlanoramaHelper.getInstance(getActivity());

        listView.setAdapter(new MyAdapter(planoramaHelper.getmSOSList()));

        return view;
    }


    private class MyAdapter extends BaseAdapter {
        private final ArrayList<SOSBO> items;

        public MyAdapter(ArrayList<SOSBO> items) {
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
                        R.layout.row_planorama_sos, parent, false);

                row.setTag(holder);

                holder.textView_productName = row
                        .findViewById(R.id.tvProductNameTitle);
                holder.textView_actual = row
                        .findViewById(R.id.textView_actual);
                holder.textView_target = row
                        .findViewById(R.id.textView_target);



            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.planoramaProductBO = items.get(position);

            holder.textView_productName.setText(holder.planoramaProductBO.getProductName());

            holder.textView_actual.setText(holder.planoramaProductBO.getLocations().get(0).getPercentage()+" ("+holder.planoramaProductBO.getLocations().get(0).getActual()+"/"+holder.planoramaProductBO.getLocations().get(0).getParentTotal()+")");
            holder.textView_target.setText(String.valueOf(holder.planoramaProductBO.getNorm()));



            return row;
        }
    }

    class ViewHolder {
        SOSBO planoramaProductBO;
        TextView textView_productName,textView_actual,textView_target;

    }

}
