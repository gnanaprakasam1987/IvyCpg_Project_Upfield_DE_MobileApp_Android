package com.ivy.cpg.view.reports.contractreport;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ContractReportFragment extends IvyBaseFragment {
    FrameLayout drawer;
    CompositeDisposable compositeDisposable;
    Unbinder unbinder;

    @BindView(R.id.listView_contract)
    ListView listView;

    @BindView(R.id.labelSNO)
    TextView snoLabelTV;

    @BindView(R.id.labelOutletCode)
    TextView outletCodeLabelTV;

    @BindView(R.id.labelOutletName)
    TextView outletNameLabelTV;

    @BindView(R.id.labelSubChannel)
    TextView subChannelLabelTV;

    @BindView(R.id.labelContractID)
    TextView contractIdLabelTV;

    @BindView(R.id.labelTradeType)
    TextView tradeLabelTV;

    @BindView(R.id.labelStartDate)
    TextView startDateLabelTV;

    @BindView(R.id.labelEndDate)
    TextView endDateLabelTV;

    @BindView(R.id.labelNoOfDays)
    TextView noOfDaysLabelTV;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contract_report, container,
                false);
        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        unbinder = ButterKnife.bind(this, view);

        snoLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        outletCodeLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        outletNameLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        subChannelLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        contractIdLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        tradeLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        startDateLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        endDateLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        noOfDaysLabelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();

        }

        return view;
    }

    private void getContractDate() {
        final AlertDialog alertDialog;
        /*AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
        /*customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/
        compositeDisposable.add((Disposable) ContractReportHelper.getInstance().downloadContractReport(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<ContractBO>>() {
                    @Override
                    public void onNext(ArrayList<ContractBO> contractList) {
                        if (contractList.size() > 0) {
                            Collections.sort(contractList, ContractBO.DayToExpiryComparator);
                            MyAdapter adapter = new MyAdapter(contractList);
                            listView.setAdapter(adapter);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getContractDate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed())
            compositeDisposable.clear();

        unbinder.unbind();
    }


    class MyAdapter extends BaseAdapter {
        ArrayList<ContractBO> arrayList;

        public MyAdapter(ArrayList<ContractBO> conList) {
            arrayList = conList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public ContractBO getItem(int arg0) {
            return arrayList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.contract_report_list_item, parent, false);
                holder = new ViewHolder(convertView);

                holder.snoTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.outletCodeTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.outletNameTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.subChannelTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.contractIdTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.tradeTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.startDateTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.endDateTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.noOfDaysTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ContractBO contractBO = arrayList.get(position);
            int sno = position + 1;
            holder.snoTV.setText(String.valueOf(sno));
            holder.outletCodeTV.setText(contractBO.getOutletCode());
            holder.outletNameTV.setText(contractBO.getOutletName());
            holder.subChannelTV.setText(contractBO.getSubChannel());
            holder.contractIdTV.setText(contractBO.getContractID());
            holder.tradeTV.setText(contractBO.getTradeName());
            holder.startDateTV.setText(contractBO.getStartDate());
            holder.endDateTV.setText(contractBO.getEndDate());
            holder.noOfDaysTV.setText(String.valueOf(contractBO.getDaysToExp()));
            return convertView;
        }

    }

    class ViewHolder {
        @BindView(R.id.tvSNO)
        TextView snoTV;

        @BindView(R.id.tvOutletCode)
        TextView outletCodeTV;

        @BindView(R.id.tvOutletName)
        TextView outletNameTV;

        @BindView(R.id.tvSubChannel)
        TextView subChannelTV;

        @BindView(R.id.tvContractId)
        TextView contractIdTV;

        @BindView(R.id.tvTradeType)
        TextView tradeTV;

        @BindView(R.id.tvStartDate)
        TextView startDateTV;

        @BindView(R.id.tvEndDate)
        TextView endDateTV;

        @BindView(R.id.tvNoOfDays)
        TextView noOfDaysTV;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }


    }
}
