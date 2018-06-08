package com.ivy.cpg.view.reports.dayreport;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.Vector;


public class MyAdapter extends BaseAdapter {

    private Vector<ConfigureBO> items;
    private BusinessModel mBusinessModel;

    public MyAdapter(Vector<ConfigureBO> conList, BusinessModel businessModel) {
        items = conList;
        this.mBusinessModel = businessModel;
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.row_daily_report_fragment, parent, false);
            holder = new ViewHolder();

            holder.name = convertView.findViewById(R.id.name_txt);
            holder.value = convertView.findViewById(R.id.value_txt);
            holder.value1 = convertView.findViewById(R.id.value_txt1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(items.get(position).getMenuName());
        String menuNumber = items.get(position).getMenuNumber();
        if (menuNumber.contains("/")) {
            String a1 = menuNumber.split("/")[0];
            String b1 = menuNumber.split("/")[1];
            holder.value.setText(a1);
            holder.value1.setText("/" + b1);

        } else {
            holder.value.setText(menuNumber);

        }
        holder.value.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.name.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.value1.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        return convertView;
    }

    public class ViewHolder {
        TextView name, value, value1;
    }


}



