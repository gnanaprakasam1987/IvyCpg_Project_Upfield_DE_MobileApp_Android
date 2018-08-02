package com.ivy.cpg.view.reports.distorderreport;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class DistOrderReportFragment extends IvyBaseFragment implements OnClickListener,
        OnItemClickListener {

    private BusinessModel bmodel;
    private ArrayList<DistOrderReportBo> mylist;
    private DistOrderReportBo mSelectedReportBO;
    private Unbinder unbinder;
    private CompositeDisposable compositeDisposable;

    @BindView(R.id.list)
    ListView lvwplist;

    @BindView(R.id.txttotal)
    TextView totalOrderValue;

    @BindView(R.id.txtavglines)
    TextView averageLines;

    @BindView(R.id.lpc)
    TextView mlpc;

    @BindView(R.id.txt_dist_pre_post)
    TextView mavg_pre_post;

    @BindView(R.id.txttotallines)
    TextView totalLines;

    @BindView(R.id.lbl_avg_lines)
    TextView lblAvgLines;

    @BindView(R.id.lbl_total_lines)
    TextView lblTotLines;

    @BindView(R.id.lab_dist_pre_post)
    TextView lblDistPrePost;

    @BindView(R.id.dist)
    TextView distTv;

    @BindView(R.id.outna)
    TextView outletName;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_order_report, container,
                false);
        unbinder = ButterKnife.bind(this, view);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        updateViews();

        DistOrderReportHelper distOrderReportHelper = DistOrderReportHelper.getInstance();

        getDistOrdReportData(distOrderReportHelper);

        return view;

    }

    /**
     * Disable View's based on confgi
     * add labels
     */
    private void updateViews() {
        lvwplist.setCacheColorHint(0);
        lvwplist.setOnItemClickListener(this);

        if (!bmodel.configurationMasterHelper.SHOW_TOTAL_LINES)
            totalLines.setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            lblAvgLines.setVisibility(View.GONE);
            averageLines.setVisibility(View.GONE);
            // mlpc.setVisibility(View.GONE);
        }
        if (!bmodel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            totalLines.setVisibility(View.GONE);
            lblTotLines.setVisibility(View.GONE);

        }
        if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            lblDistPrePost.setVisibility(View.GONE);
            mavg_pre_post.setVisibility(View.GONE);
            distTv.setVisibility(View.GONE);

        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(outletName.getTag()) != null)
                outletName.setText(bmodel.labelsMasterHelper
                        .applyLabels(outletName.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(mlpc.getTag()) != null)
                mlpc.setText(bmodel.labelsMasterHelper.applyLabels(mlpc.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    double avgLine = 0, totOutlet = 0;

    private void getDistOrdReportData(DistOrderReportHelper distOrderReportHelper) {
        final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        compositeDisposable = new CompositeDisposable();
        customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();
        compositeDisposable.add((Disposable) Observable.zip(distOrderReportHelper.downloadDistributorOrderReport(getActivity()), distOrderReportHelper.getavglinesfororderbooking("OrderHeader", getActivity())
                , distOrderReportHelper.getorderbookingCount("OrderHeader", getActivity())
                , new Function3<ArrayList<DistOrderReportBo>, Double, Double, Object>() {

                    @Override
                    public Object apply(ArrayList<DistOrderReportBo> distOrderReportBos, Double avgLines, Double totOutlets) throws Exception {
                        mylist.addAll(distOrderReportBos);
                        avgLine = avgLines;
                        totOutlet = totOutlets;
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        // Show alert if no order exist.
                        if (mylist.size() == 0) {
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.no_orders_available),
                                    Toast.LENGTH_SHORT).show();
                        }
                        updateOrderGrid(avgLine, totOutlet);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Show alert if error loading data.
                        alertDialog.dismiss();
                        if (mylist == null) {
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.unable_to_load_data),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onComplete() {
                        alertDialog.dismiss();
                    }
                }));
    }

    public void onClick(View comp) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void updateOrderGrid(double avgLine, double totOutlet) {
        double totalvalue = 0;
        int pre = 0, post = 0;

        // Calculate the total order value.
        for (DistOrderReportBo ret : mylist) {
            totalvalue = totalvalue + ret.getOrderTotal();
        }

        if (bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            // Calculate the total order value.
            for (DistOrderReportBo ret : mylist) {
                try {
                    String str[] = ret.getDist().split("/");
                    pre = pre + SDUtil.convertToInt(str[0]);
                    post = post + SDUtil.convertToInt(str[1]);
                } catch (Exception e) {
                    // TODO: handle exception
                    Commons.printException(e);
                }

            }
            float preavg = 0, postavg = 0;
            if (mylist.size() > 0) {
                if (pre > 0) {
                    preavg = (float) pre / (float) mylist.size();
                }
                if (post > 0) {
                    postavg = (float) post / (float) mylist.size();
                }

                mavg_pre_post.setText(SDUtil.format(preavg, 1, 0) + "/"
                        + SDUtil.format(postavg, 1, 0));

            } else {
                mavg_pre_post.setText("0/0");
            }

        }
        // Format and set on the lable
        totalOrderValue.setText("" + bmodel.formatValue(totalvalue));
        if (bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            double result = avgLine / totOutlet;
            String resultS = result + "";
            if (resultS.equals(getResources().getString(R.string.nan))) {
                averageLines.setText("" + 0);
            } else {
                averageLines.setText("" + SDUtil.roundIt(result, 2));
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_TOTAL_LINES)
            totalLines.setText(totOutlet + "");


        // Load listview.
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);

    }


    class MyAdapter extends ArrayAdapter<DistOrderReportBo> {
        ArrayList<DistOrderReportBo> items;

        private MyAdapter(ArrayList<DistOrderReportBo> items) {
            super(getActivity(), R.layout.row_order_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            DistOrderReportBo orderreport = (DistOrderReportBo) items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_order_report, parent, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            // if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            // holder.tvwlpc.setVisibility(View.GONE);
            // }
            if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                holder.tvwDist.setVisibility(View.GONE);

            }

            holder.tvwrname.setText(orderreport.getDistributorName());
            holder.tvwvalue.setText(bmodel.formatValue((orderreport
                    .getOrderTotal())) + "");
            holder.tvwlpc.setText(orderreport.getLpc());
            holder.tvwDist.setText(orderreport.getDist());
            holder.tvOrderNo.setText(orderreport.getOrderId());
            ;

            if (orderreport.getUpload().equalsIgnoreCase("Y")) {
                holder.tvwrname.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.tvwvalue.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.tvwlpc.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.tvwDist.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.tvOrderNo.setTextColor(getResources().getColor(
                        R.color.GREEN));

            } else {

                row.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.list_selector));
            }
            return (row);
        }
    }

    class ViewHolder {
        String ref;// product id

        @BindView(R.id.PRDNAME)
        TextView tvwrname;

        @BindView(R.id.PRDNAME)
        TextView tvwvol;

        @BindView(R.id.PRDMRP)
        TextView tvwvalue;

        @BindView(R.id.PRDRP)
        TextView tvwlpc;

        @BindView(R.id.dist_txt)
        TextView tvwDist;

        @BindView(R.id.orderno)
        TextView tvOrderNo;

        ViewHolder(View view) {
            ButterKnife.bind(view);
        }


    }


    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        try {

            DistOrderReportBo ret = (DistOrderReportBo) mylist
                    .get(arg2);
            bmodel.reportHelper.updateDistributor(ret.getDistributorId() + "");
            bmodel.productHelper
                    .downloadDistributorProducts("MENU_PS_STKORD");

            Intent orderreportdetail = new Intent();
            orderreportdetail.putExtra("OBJ",
                    ret);
            orderreportdetail.putExtra("isFromOrder", true);
            orderreportdetail.putExtra("TotalValue", ret.getOrderTotal());
            orderreportdetail.putExtra("TotalLines", ret.getLpc());
            orderreportdetail.setClass(getActivity(), DistOrderreportdetail.class);
            startActivityForResult(orderreportdetail, 0);

        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    public void onBackPressed() {
        // do something on back.
        return;
    }

    class XlsExport extends AsyncTask<Void, Void, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, "Exporting orders...", true, false);*/
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, "Exporting orders...");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            alertDialog.dismiss();
            //	progressDialogue.dismiss();
            if (result)
                Toast.makeText(getActivity(), "Sucessfully Exported.",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "Export Failed.",
                        Toast.LENGTH_SHORT).show();
        }

    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Do you want to delete Invoice")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

        }
        return null;
    }

}
