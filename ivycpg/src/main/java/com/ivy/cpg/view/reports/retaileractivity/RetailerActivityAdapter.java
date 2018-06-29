package com.ivy.cpg.view.reports.retaileractivity;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

/**
 * Created by abbas.a on 25/06/18.
 */

public class RetailerActivityAdapter extends BaseAdapter {


    private ArrayList<RetailerMasterBO> items;
    private BusinessModel bmodel;

    public RetailerActivityAdapter(ArrayList<RetailerMasterBO> items, BusinessModel businessModel) {
        this.items = items;
        this.bmodel=businessModel;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        RetailerMasterBO volumereport = items
                .get(position);
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            row = inflater.inflate(R.layout.row_retailer_activity_report, parent,
                    false);
            holder = new ViewHolder();

            row.findViewById(R.id.view_dotted_line).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            holder.storeName =  row.findViewById(R.id.storename);
            holder.visitTarget =  row.findViewById(R.id.vtgt);
            holder.visitActual = row.findViewById(R.id.vact);
            holder.vGap = row.findViewById(R.id.vgap);
            holder.linesSold = row.findViewById(R.id.linessold);
            holder.startTime = row.findViewById(R.id.start);
            holder.endTime = row.findViewById(R.id.end);
            holder.duration = row.findViewById(R.id.duration);

            holder.label_Target = row.findViewById(R.id.label_tgt);
            holder.label_Actual = row.findViewById(R.id.label_ach);
            holder.label_Gap = row.findViewById(R.id.label_gap);
            holder.label_linesSold = row.findViewById(R.id.label_line_sold);
            holder.label_startTime = row.findViewById(R.id.label_start);
            holder.label_endTime = row.findViewById(R.id.label_end);
            holder.label_duration = row.findViewById(R.id.label_duration);

            holder.storeName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.visitTarget.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.visitActual.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.vGap.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.linesSold.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.startTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.endTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.duration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.label_Target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.label_Actual.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.label_Gap.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.label_linesSold.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.label_startTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.label_endTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.label_duration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.storeName.setText(volumereport.getRetailerName().trim());
        holder.linesSold.setText(volumereport.getTotalLines() + "");
        holder.startTime.setText(volumereport.getRField1() != null ? volumereport.getRField1().trim() : "-");
        holder.endTime.setText(volumereport.getRfield2() != null ? volumereport.getRfield2().trim() : "-");

        holder.visitTarget.setText("" + SDUtil.format(volumereport
                .getDaily_target(), 0, 0));
        holder.visitActual.setText("" + SDUtil.format(volumereport
                .getVisit_Actual(), 0, 0));

        double gap = (volumereport.getDaily_target() - volumereport.getVisit_Actual());

        String gapValue = SDUtil.format(gap, 0, 0)
                + "".trim();
        String percentValue = "";


        if (volumereport.getDaily_target() > 0) {
            double percent = ((volumereport
                    .getVisit_Actual() / volumereport.getDaily_target()) * 100);
            if (percent < 100) {
                percentValue = bmodel.formatValue(percent) + "";
            } else {
                percentValue = "100";
            }
        } else {
            percentValue = "0";
        }

        holder.vGap.setText(gapValue + "(" + percentValue + "%)");
        holder.duration.setText((volumereport.getRField4() != null) ? volumereport.getRField4() : "0");

        return (row);
    }
}

class ViewHolder {
    TextView storeName, visitTarget, visitActual, vGap, linesSold, startTime, endTime, duration;
    TextView label_Target, label_Actual, label_Gap, label_linesSold, label_startTime, label_endTime, label_duration;

}

