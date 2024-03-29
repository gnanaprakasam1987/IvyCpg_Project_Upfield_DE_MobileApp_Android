package com.ivy.cpg.view.reports.deliveryStockReport;

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

/**
 * Created by rajkumar.s on 12/1/2016.
 */

public class DeliveryStockReport extends IvyBaseFragment implements View.OnClickListener {
    private Unbinder unbinder;
    BusinessModel bmodel;
    private CompositeDisposable compositeDisposable;

    @BindView(R.id.list)
    ListView listView;

    @BindView(R.id.piece_qty)
    TextView pieceTitleTv;

    @BindView(R.id.case_qty)
    TextView caseTitleTv;

    @BindView(R.id.outer_qty)
    TextView outerTitleTv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_report, container,
                false);

        try {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());

            unbinder = ButterKnife.bind(this, view);

            try {
                if (bmodel.labelsMasterHelper.applyLabels(caseTitleTv.getTag()) != null)
                    caseTitleTv
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(caseTitleTv.getTag()));

                if (bmodel.labelsMasterHelper.applyLabels(pieceTitleTv.getTag()) != null)
                    pieceTitleTv
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(pieceTitleTv.getTag()));

                if (bmodel.labelsMasterHelper.applyLabels(outerTitleTv.getTag()) != null)
                    outerTitleTv
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(outerTitleTv.getTag()));

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    caseTitleTv.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    pieceTitleTv.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    outerTitleTv.setVisibility(View.GONE);

            } catch (Exception e) {
                Commons.printException(e);
            }

            getDeliverStockData();


        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return view;
    }

    /**
     * load delivery stock data
     */
    private void getDeliverStockData() {
        final AlertDialog alertDialog;
        /*AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
       /* customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/
        compositeDisposable.add((Disposable) DeliveryStockHelper.getInstance().downloadDeliveryStock(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<DeliveryStockBo>>() {
                    @Override
                    public void onNext(ArrayList<DeliveryStockBo> deliveryStockList) {
                        if (deliveryStockList.size() > 0) {
                            MyAdapter adapter = new MyAdapter(deliveryStockList);
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
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        unbinder.unbind();
    }

    class MyAdapter extends ArrayAdapter<DeliveryStockBo> {
        ArrayList<DeliveryStockBo> items;

        MyAdapter(ArrayList<DeliveryStockBo> items) {
            super(getActivity(), R.layout.row_delivery_report,
                    items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_delivery_report, parent,
                        false);
                holder = new ViewHolder(row);
                holder.tvwpsname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tv_case_qty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tv_piece_qty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.tv_outer_qty.setVisibility(View.GONE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.deliveryStkBO = items.get(position);

            holder.tvwpsname.setText(holder.deliveryStkBO.getProductShortName());

            holder.tv_case_qty.setText(holder.deliveryStkBO.getOrderedCaseQty() + "");
            holder.tv_outer_qty.setText(holder.deliveryStkBO.getOrderedOuterQty() + "");
            holder.tv_piece_qty.setText(holder.deliveryStkBO.getOrderedPcsQty() + "");


            return (row);
        }
    }

    class ViewHolder {
        @BindView(R.id.product_name_title)
        TextView tvwpsname;

        @BindView(R.id.case_qty)
        TextView tv_case_qty;

        @BindView(R.id.outer_qty)
        TextView tv_outer_qty;

        @BindView(R.id.piece_qty)
        TextView tv_piece_qty;

        ViewHolder(View view) {
            ButterKnife.bind(this,view);
        }

        DeliveryStockBo deliveryStkBO;
    }

    @Override
    public void onClick(View view) {

    }
}
