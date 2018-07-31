package com.ivy.cpg.view.reports.questionReport;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class QuestionReportFragment extends Fragment {
    private BusinessModel bmodel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questionreport, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        ListView lvwplist = (ListView) view.findViewById(R.id.lv_availcheckreport_list);

        Vector<QuestionReportBO> mylist = new Vector<QuestionReportBO>();

        mylist.addAll(new QuestionReportHelper(getActivity()).loadQuestionReport());

        MyAdapter mSchedule = new MyAdapter(mylist);

        lvwplist.setAdapter(mSchedule);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    private QuestionReportBO avlChkReport;

    private class MyAdapter extends ArrayAdapter<QuestionReportBO> {
        private Vector<QuestionReportBO> items;

        public MyAdapter(Vector<QuestionReportBO> items) {
            super(getActivity(), R.layout.row_questionreport, items);
            this.items = items;
        }

        public QuestionReportBO getItem(int position) {
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
            avlChkReport = (QuestionReportBO) items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_questionreport, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) row.findViewById(R.id.tv_avail_checkretailText);
                holder.v1 = (TextView) row.findViewById(R.id.tv_avail_checkretailV1);
                holder.v2 = (TextView) row.findViewById(R.id.tv_avail_checkretailV2);
                holder.v3 = (TextView) row.findViewById(R.id.tv_avail_checkretailV3);
                holder.v4 = (TextView) row.findViewById(R.id.tv_avail_checkretailV4);
                holder.Percentage = (TextView) row.findViewById(R.id.tv_avail_checkretailPercentage);

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.text.setText(items.get(position).getText());
            holder.v1.setText(items.get(position).getV1() + "");
            holder.v2.setText(items.get(position).getV2() + "");
            holder.v3.setText(items.get(position).getV3() + "");
            holder.v4.setText(items.get(position).getV4() + "");
            Commons.print("getV4 : " + items.get(position).getV4());
            Commons.print("getV2 : " + items.get(position).getV2());


            int visitedOutletCount = items.get(position).getV3();
            int plannedOutletCount = items.get(position).getV2();
            int availableOutletCount = items.get(position).getV4();

            double result;

            if (visitedOutletCount != 0 && availableOutletCount != 0)
                result = ((double) availableOutletCount / (double) visitedOutletCount) * 100;
            else
                result = 0;


            holder.Percentage.setText(bmodel.formatPercent(result) + "");
            return row;
        }
    }

    class ViewHolder {
        TextView text, v1, v2, v3, v4, Percentage;
    }


}
