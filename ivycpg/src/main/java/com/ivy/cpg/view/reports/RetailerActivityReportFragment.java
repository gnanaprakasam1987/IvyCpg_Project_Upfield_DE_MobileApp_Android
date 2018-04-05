package com.ivy.cpg.view.reports;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class RetailerActivityReportFragment extends Fragment {
    private BusinessModel bmodel;
    private View view;
    private ListView listview;
    private Vector<RetailerMasterBO> volumereport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_retailer_activity_report, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        listview = (ListView) view.findViewById(R.id.lvwplist);
        listview.setCacheColorHint(0);
        volumereport = downloadRetailerActivityReport();

        MyAdapter mSchedule = new MyAdapter(volumereport);
        listview.setAdapter(mSchedule);
        // Load listview.

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private Vector<RetailerMasterBO> downloadRetailerActivityReport() {
        int index = 1;
        Vector<RetailerMasterBO> retailer = new Vector<>();
        try {
            RetailerMasterBO retailerbo, ret;
            ret = new RetailerMasterBO();

            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            for (RetailerMasterBO retailerList : bmodel.getRetailerMaster()) {
                if (retailerList.getIsToday() == 1 || retailerList.getIsDeviated().equalsIgnoreCase("Y")) {
                    retailerbo = new RetailerMasterBO();
                    retailerbo.setRetailerID(retailerList.getRetailerID());
                    retailerbo.setRetailerName(retailerList.getRetailerName());
                    retailerbo.setWalkingSequence(index);
                    index++;
                    retailer.add(retailerbo);

                }
            }
            Cursor c = db.selectSQL("select RetailerID, sum(OrderValue),LinesPerCall from OrderHeader group by retailerid");

            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < retailer.size(); i++) {
                        ret = retailer.get(i);
                        if (ret.getRetailerID().equals(c.getString(0))) {
                            ret.setVisit_Actual(c.getDouble(1));
                            ret.setTotalLines(c.getInt(2));
                            Commons.print("VIit actual" + c.getDouble(1) + "OBJ" + ret.getTotalLines());
                            Commons.print("VIit Line" + c.getInt(2));
                        }
                    }
                }
                c.close();
            }
            c = db.selectSQL("select RetailerID, DailyTarget from RetailerTargetMaster ");

            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < retailer.size(); i++) {
                        ret = retailer.get(i);
                        if (ret.getRetailerID().equals(c.getString(0))) {
                            ret.setDaily_target(c.getDouble(1));
                            Commons.print("VIit tgt" + c.getDouble(1));
                        }
                    }
                }
                c.close();
            }
            c = db.selectSQL("select retailerid,substr(timeIn,12,5),substr(timeout,12,5),timein,timeout from OutletTimestamp group by retailerid");

            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < retailer.size(); i++) {
                        ret = retailer.get(i);
                        if (ret.getRetailerID().equals(c.getString(0))) {
                            ret.setRField1(c.getString(1));
                            ret.setRfield2(c.getString(2));
                            ret.setRField4(timeDifference(c.getString(3), c.getString(4)));
                            Commons.print("Time in" + c.getString(1));
                            Commons.print("Time out" + c.getString(2));
                        }
                    }
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
        return retailer;

    }

    public String timeDifference(String sTime, String eTime) {
        String duration = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(sTime);
            d2 = format.parse(eTime);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            //  long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays > 0) {
                duration += diffDays + " ";
            }
            if (diffHours > 0) {
                if (!duration.equals(""))
                    duration += ":";

                duration += diffHours;
            }

            if (diffMinutes > 0) {
                if (!duration.equals(""))
                    duration += ":";
                if (duration.equals(""))
                    duration += "00:";

                duration += diffMinutes;
                int fullLen = duration.length();//1:2
                int len = duration.substring(duration.indexOf(":")).length();

                if(len==2)
                {
                if(fullLen==3)
                    duration=duration.substring(0,1)+":0"+duration.substring(2);
                    else
                    duration=duration.substring(0,2)+":0"+duration.substring(3);

                }
            }
          /*  if(diffSeconds>0){
                if(!duration.equals(""))
                    duration+=":";

                duration+=diffSeconds;
            }*/

            if (duration.equals(""))
                duration = "0";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return duration;
    }

    class MyAdapter extends BaseAdapter {
        Vector<RetailerMasterBO> items;

        public MyAdapter(Vector<RetailerMasterBO> items) {
            //  super(getActivity(), R.layout.row_retailer_activity_report, items);
            this.items = items;
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

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            RetailerMasterBO volumereport = items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_retailer_activity_report, parent,
                        false);
                holder = new ViewHolder();

                ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                holder.storename = (TextView) row.findViewById(R.id.storename);
                holder.vTarget = (TextView) row.findViewById(R.id.vtgt);
                holder.vActual = (TextView) row.findViewById(R.id.vact);
                holder.vGap = (TextView) row.findViewById(R.id.vgap);
               // holder.vPerc = (TextView) row.findViewById(R.id.vperc);
                holder.linesSold = (TextView) row.findViewById(R.id.linessold);
                holder.startTime = (TextView) row.findViewById(R.id.start);
                holder.endTime = (TextView) row.findViewById(R.id.end);
                holder.duration = (TextView) row.findViewById(R.id.duration);

                holder.label_Target = (TextView) row.findViewById(R.id.label_tgt);
                holder.label_Actual = (TextView) row.findViewById(R.id.label_ach);
                holder.label_Gap = (TextView) row.findViewById(R.id.label_gap);
                holder.label_linesSold = (TextView) row.findViewById(R.id.label_line_sold);
                holder.label_startTime = (TextView) row.findViewById(R.id.label_start);
                holder.label_endTime = (TextView) row.findViewById(R.id.label_end);
                holder.label_duration = (TextView) row.findViewById(R.id.label_duration);

                holder.storename.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.vTarget.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.vActual.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
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

           /* holder.sNo.setText(volumereport.getWalkingSequence()
                    + "".trim());*/
            holder.storename.setText(volumereport.getRetailerName().trim());
            holder.linesSold.setText(volumereport.getTotalLines() + "");
            holder.startTime.setText(volumereport.getRField1() != null ? volumereport.getRField1().trim() : "-");
            holder.endTime.setText(volumereport.getRfield2() != null ? volumereport.getRfield2().trim() : "-");
            Commons.print("Percentage" + (((volumereport.getDaily_target() - volumereport.getVisit_Actual()) / volumereport.getDaily_target())) * 100);

            holder.vTarget.setText("" + SDUtil.format(volumereport
                    .getDaily_target(), 0, 0));
            holder.vActual.setText("" + SDUtil.format(volumereport
                    .getVisit_Actual(), 0, 0));

            double gap = (volumereport.getDaily_target() - volumereport.getVisit_Actual());

            String gapValue=SDUtil.format(gap, 0, 0)
                    + "".trim();
            String percentValue="";


            if (volumereport.getDaily_target() > 0) {
                double percent = ((volumereport
                        .getVisit_Actual() / volumereport.getDaily_target()) * 100);
                if (percent < 100) {
                    percentValue=bmodel.formatValue(percent) + "";
                } else {
                    percentValue="100";
                }
            } else {
                percentValue="0";
            }

            holder.vGap.setText(gapValue+"("+percentValue+"%)");
            holder.duration.setText((volumereport.getRField4() != null) ? volumereport.getRField4() : "0");

            return (row);
        }
    }

    class ViewHolder {
        TextView sNo, storename, vTarget, vActual, vGap, vPerc, linesSold, startTime, endTime, duration;

        TextView  label_Target, label_Actual, label_Gap, label_Perc, label_linesSold, label_startTime, label_endTime, label_duration;

    }


}
