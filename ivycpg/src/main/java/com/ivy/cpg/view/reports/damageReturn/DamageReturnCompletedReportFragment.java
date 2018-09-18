package com.ivy.cpg.view.reports.damageReturn;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by murugan on 17/9/18.
 */

public class DamageReturnCompletedReportFragment extends IvyBaseFragment {

    CompositeDisposable compositeDisposable;
    Unbinder unbinder;

    @BindView(R.id.pending_delivery_listview)
    ListView listView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_damage_return, container,
                false);
        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        unbinder = ButterKnife.bind(this, view);

        // textView.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

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
        compositeDisposable.add((Disposable) DamageReturenReportHelper.getInstance().downloadPendingDeliveryStatusReport(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<PandingDeliveryBO>>() {
                    @Override
                    public void onNext(ArrayList<PandingDeliveryBO> contractList) {
                        if (contractList.size() > 0) {

                            DamageReturnCompletedReportFragment.MyAdapter adapter = new DamageReturnCompletedReportFragment.MyAdapter(contractList);
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
        ArrayList<PandingDeliveryBO> arrayList;

        public MyAdapter(ArrayList<PandingDeliveryBO> conList) {
            arrayList = conList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public PandingDeliveryBO getItem(int arg0) {
            return arrayList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DamageReturnCompletedReportFragment.ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.pending_delivery_list_item_, parent, false);
                holder = new DamageReturnCompletedReportFragment.ViewHolder(convertView);

                holder.statusTitle.setVisibility(View.VISIBLE);
                holder.status.setVisibility(View.VISIBLE);

                holder.invoiceNoTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.invoiceNo.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.invoiceDateTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.invoiceDate.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.invNetamounTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.invNetamount.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.storeNameTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.txtStorename.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.statusTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.status.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                convertView.setTag(holder);
            } else {
                holder = (DamageReturnCompletedReportFragment.ViewHolder) convertView.getTag();
            }
            PandingDeliveryBO pandingDeliveryBO = arrayList.get(position);

            holder.invoiceNo.setText(pandingDeliveryBO.getInvoiceNo());
            holder.invoiceDate.setText(pandingDeliveryBO.getInvoiceDate());
            holder.invNetamount.setText(pandingDeliveryBO.getInvNetamount());
            holder.txtStorename.setText(pandingDeliveryBO.getRetailerName());

            if("P".equalsIgnoreCase(pandingDeliveryBO.getStatus())){
                holder.status.setText("Partially Delivered");
                holder.status.setTextColor(getResources().getColor(R.color.red_week_background));
            }else if("F".equalsIgnoreCase(pandingDeliveryBO.getStatus())){
                holder.status.setText("Delivered");
                holder.status.setTextColor(getResources().getColor(R.color.select_week_color_green));
            }else{
                holder.status.setText("Rejected");
                holder.status.setTextColor(getResources().getColor(R.color.pink_week_background));
            }

            return convertView;
        }

    }

    class ViewHolder {
        @BindView(R.id.invoiceNo_title)
        TextView invoiceNoTitle;

        @BindView(R.id.txtInvoiceNo)
        TextView invoiceNo;

        @BindView(R.id.invoiceDate_title)
        TextView invoiceDateTitle;

        @BindView(R.id.txtInvoiceDate)
        TextView invoiceDate;

        @BindView(R.id.invNetamount_title)
        TextView invNetamounTitle;

        @BindView(R.id.txtInvNetamount)
        TextView invNetamount;

        @BindView(R.id.txtStorename)
        TextView txtStorename;

        @BindView(R.id.storename_title)
        TextView storeNameTitle;

        @BindView(R.id.status_title)
        TextView statusTitle;

        @BindView(R.id.txtStatus)
        TextView status;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }


    }
}
