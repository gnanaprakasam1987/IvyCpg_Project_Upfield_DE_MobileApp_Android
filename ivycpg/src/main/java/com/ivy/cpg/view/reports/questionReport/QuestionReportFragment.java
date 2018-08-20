package com.ivy.cpg.view.reports.questionReport;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class QuestionReportFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private CompositeDisposable compositeDisposable;
    private Unbinder unbinder;

    @BindView(R.id.lv_availcheckreport_list)
    ListView lvwplist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questionreport, container, false);

        unbinder = ButterKnife.bind(this,view);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getQuestionData();
    }


    private void getQuestionData() {
        /*final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
        /*customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/

        compositeDisposable.add((Disposable) QuestionReportHelper.getInstance().loadQuestionReports(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<QuestionReportBO>>() {
                    @Override
                    public void onNext(ArrayList<QuestionReportBO> questionReportList) {
                        if (questionReportList.size() > 0) {
                            MyAdapter mSchedule = new MyAdapter(questionReportList);
                            lvwplist.setAdapter(mSchedule);
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //alertDialog.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_data), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        //alertDialog.dismiss();
                    }
                }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        unbinder.unbind();
    }

    private class MyAdapter extends ArrayAdapter<QuestionReportBO> {
        private ArrayList<QuestionReportBO> items;

        public MyAdapter(ArrayList<QuestionReportBO> items) {
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

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_questionreport, parent, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.avlChkReport = (QuestionReportBO) items.get(position);
            holder.text.setText(holder.avlChkReport.getText());
            holder.v1.setText(holder.avlChkReport.getV1() + "");
            holder.v2.setText(holder.avlChkReport.getV2() + "");
            holder.v3.setText(holder.avlChkReport.getV3() + "");
            holder.v4.setText(holder.avlChkReport.getV4() + "");
            Commons.print("getV4 : " + holder.avlChkReport.getV4());
            Commons.print("getV2 : " + holder.avlChkReport.getV2());


            int visitedOutletCount = holder.avlChkReport.getV3();
            int plannedOutletCount = holder.avlChkReport.getV2();
            int availableOutletCount = holder.avlChkReport.getV4();

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
        private QuestionReportBO avlChkReport;

        @BindView(R.id.tv_avail_checkretailText)
        TextView text;

        @BindView(R.id.tv_avail_checkretailV1)
        TextView v1;

        @BindView(R.id.tv_avail_checkretailV2)
        TextView v2;

        @BindView(R.id.tv_avail_checkretailV3)
        TextView v3;

        @BindView(R.id.tv_avail_checkretailV4)
        TextView v4;

        @BindView(R.id.tv_avail_checkretailPercentage)
        TextView Percentage;

        ViewHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }


}
