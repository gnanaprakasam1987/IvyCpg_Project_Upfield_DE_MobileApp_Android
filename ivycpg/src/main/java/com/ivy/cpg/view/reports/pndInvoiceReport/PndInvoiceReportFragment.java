package com.ivy.cpg.view.reports.pndInvoiceReport;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PndInvoiceReportFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private CompositeDisposable compositeDisposable;
    private Unbinder unbinder;

    @BindView(R.id.list)
    ListView lvwplist;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_pnd_invoice_report,
                container, false);
        unbinder = ButterKnife.bind(this,view);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        lvwplist = (ListView) view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        getPndInvoiceData();

        return view;
    }

    private void getPndInvoiceData() {
        /*final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
       /* customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/

        compositeDisposable.add((Disposable) PendingInvoiceHelper.getInstance().downloadPndInvoice(getActivity().getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<PndInvoiceReportBo>>() {
                    @Override
                    public void onNext(ArrayList<PndInvoiceReportBo> pndInvList) {
                        if (pndInvList.size() > 0) {
                            MyAdapter mSchedule = new MyAdapter(pndInvList);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed())
            compositeDisposable.dispose();
        unbinder.unbind();
    }

    class MyAdapter extends ArrayAdapter<PndInvoiceReportBo> {
        ArrayList<PndInvoiceReportBo> items;

        private MyAdapter(ArrayList<PndInvoiceReportBo> items) {
            super(getActivity(), R.layout.row_pnd_invoice_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            PndInvoiceReportBo invoiceHeaderBO = items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_pnd_invoice_report, parent, false);
                holder = new ViewHolder(row);

                holder.tvRetailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvInvoiceNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvInvAmount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvAmtPaid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvBalance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.labelTvInvAmt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.labelTvPaidAmt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.labelTvBalanceAmt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.invoiceReportBO = invoiceHeaderBO;

            holder.tvRetailerName.setText(holder.invoiceReportBO.getRetailerName());
            holder.tvInvoiceNo.setText(holder.invoiceReportBO.getInvoiceNo());
            holder.tvDate.setText(getResources().getString(R.string.inv_date) + ":" + holder.invoiceReportBO.getInvoiceDate());
            holder.tvInvAmount.setText(bmodel.formatValue(holder.invoiceReportBO.getInvoiceAmount()));
            holder.tvAmtPaid.setText(bmodel.formatValue(holder.invoiceReportBO.getPaidAmount()));
            holder.tvBalance.setText(bmodel.formatValue(holder.invoiceReportBO.getBalance()));

            return (row);
        }
    }

    class ViewHolder {
        PndInvoiceReportBo invoiceReportBO;

        @BindView(R.id.invoiceview_doted_line)
        View invoiceview_doted_line;

        @BindView(R.id.tv_retailer_name)
        TextView tvRetailerName;

        @BindView(R.id.invoice_number)
        TextView tvInvoiceNo;

        @BindView(R.id.tvDate)
        TextView tvDate;

        @BindView(R.id.tvinvamt)
        TextView labelTvInvAmt;

        @BindView(R.id.tvinvamtValue)
        TextView tvInvAmount;

        @BindView(R.id.tvpaidamt)
        TextView labelTvPaidAmt;

        @BindView(R.id.tvpaidamtValue)
        TextView tvAmtPaid;

        @BindView(R.id.tvbalamt)
        TextView labelTvBalanceAmt;

        @BindView(R.id.tvbalamtValue)
        TextView tvBalance;

        ViewHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }


}
