package com.ivy.cpg.view.reports.closingstockreport;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.reports.promotion.RetailerNamesBO;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ClosingStockReportFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private ListView lvwplist;
    private LinearLayout linearLayout;
    private ClosingStockReportsHelper closingStockReportsHelper;
    private HashMap<String, ArrayList<ClosingStockReportBo>> retailerWiseClosingStock;
    private CompositeDisposable compositeDisposable;
    private StockCheckHelper stockCheckHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.closing_stk_report_fragment,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        closingStockReportsHelper = new ClosingStockReportsHelper(getContext());
        stockCheckHelper = StockCheckHelper.getInstance(getActivity());


        linearLayout = view.findViewById(R.id.orderScreenListRow);

        lvwplist = view.findViewById(R.id.lvwpList);
        lvwplist.setCacheColorHint(0);

        hideAndSeek(view);

        setUpLabelConfig(view);

        getClosingSTKReportData(view);

        return view;
    }

    private void getClosingSTKReportData(final View view) {
        final ArrayList<RetailerNamesBO> items = new ArrayList<>();
        final AlertDialog alertDialog;
       /* AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
        /*customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/
        compositeDisposable.add((Disposable) Observable.
                zip(closingStockReportsHelper.downloadClosingStockRetailers(),
                        closingStockReportsHelper.downloadClosingStock(),
                        new BiFunction<ArrayList<RetailerNamesBO>,
                                HashMap<String, ArrayList<ClosingStockReportBo>>, Boolean>() {
                            @Override
                            public Boolean apply(ArrayList<RetailerNamesBO> retailerNamesList,
                                                 HashMap<String, ArrayList<ClosingStockReportBo>> retailerArrayListHashMap) throws Exception {
                                if (retailerNamesList.size() > 0
                                        && retailerArrayListHashMap.size() > 0) {
                                    items.clear();
                                    items.addAll(retailerNamesList);
                                    retailerWiseClosingStock = retailerArrayListHashMap;
                                    return true;
                                } else
                                    return false;
                            }
                        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isFlag) {
                        if (isFlag) {
                            loadSpinnerView(view, items);
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


    private void loadSpinnerView(View view, ArrayList<RetailerNamesBO> items) {
        Spinner spinnerbrand = view.findViewById(R.id.spn_retailer_closing_stk);
        ArrayAdapter<RetailerNamesBO> retailerAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item);
        retailerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (RetailerNamesBO namesBO : items) {
            retailerAdapter.add(namesBO);
        }
        spinnerbrand.setAdapter(retailerAdapter);

        spinnerbrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                RetailerNamesBO reBo = (RetailerNamesBO) parent.getSelectedItem();
                updateStockReportGrid(reBo.getRetailerId());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void hideAndSeek(View view) {
        if (!stockCheckHelper.SHOW_SHELF_OUTER
                && !stockCheckHelper.SHOW_STOCK_SP
                && !stockCheckHelper.SHOW_STOCK_SC) {
            view.findViewById(R.id.case_qty).setVisibility(View.GONE);
            view.findViewById(R.id.piece_qty).setVisibility(View.GONE);
            view.findViewById(R.id.outer_qty).setVisibility(View.GONE);

        }

        if (!stockCheckHelper.SHOW_SHELF_OUTER)
            view.findViewById(R.id.outer_qty).setVisibility(View.GONE);

        if (!stockCheckHelper.SHOW_STOCK_SP)
            view.findViewById(R.id.piece_qty).setVisibility(View.GONE);

        if (!stockCheckHelper.SHOW_STOCK_SC)
            view.findViewById(R.id.case_qty).setVisibility(View.GONE);
    }

    private void setUpLabelConfig(View view) {
        try {
            ((TextView) view.findViewById(R.id.skucode)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.skucode).getTag()) != null)
                ((TextView) view.findViewById(R.id.skucode))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.skucode).getTag()));

            ((TextView) view.findViewById(R.id.skuname)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.skuname).getTag()) != null)
                ((TextView) view.findViewById(R.id.skuname))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.skuname).getTag()));

            ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.piece_qty).getTag()) != null)
                ((TextView) view.findViewById(R.id.piece_qty))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.piece_qty).getTag()));

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void updateStockReportGrid(int retailerId) {

        ArrayList<ClosingStockReportBo> closingStkList = new ArrayList<>();
        try {
            closingStkList = retailerWiseClosingStock.get(retailerId + "");
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (closingStkList == null || closingStkList.size() == 0) {
            bmodel.showAlert(getResources().getString(R.string.no_products_exists), 0);
            linearLayout.setVisibility(View.GONE);
            lvwplist.setVisibility(View.GONE);
            return;
        }

        linearLayout.setVisibility(View.VISIBLE);
        lvwplist.setVisibility(View.VISIBLE);

        MyAdapter listDatas = new MyAdapter(closingStkList);
        lvwplist.setAdapter(listDatas);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    class MyAdapter extends ArrayAdapter {

        private ArrayList<ClosingStockReportBo> items;

        public MyAdapter(ArrayList<ClosingStockReportBo> items) {
            super(getActivity(), R.layout.row_closing_stk_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            ClosingStockReportBo product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_closing_stk_report, parent, false);
                holder = new ViewHolder();

                holder.pname = row.findViewById(R.id.skuname);
                holder.pieceQty = row.findViewById(R.id.piece_qty);
                holder.caseQty = row.findViewById(R.id.case_qty);
                holder.outerQty = row.findViewById(R.id.outer_qty);
                holder.prodcode = row.findViewById(R.id.skucode);

                if (!stockCheckHelper.SHOW_SHELF_OUTER
                        && !stockCheckHelper.SHOW_STOCK_SP
                        && !stockCheckHelper.SHOW_STOCK_SC) {
                    holder.caseQty.setVisibility(View.GONE);
                    holder.pieceQty.setVisibility(View.GONE);
                    holder.outerQty.setVisibility(View.GONE);

                }

                if (!stockCheckHelper.SHOW_SHELF_OUTER)
                    holder.outerQty.setVisibility(View.GONE);

                if (!stockCheckHelper.SHOW_STOCK_SP)
                    holder.pieceQty.setVisibility(View.GONE);

                if (!stockCheckHelper.SHOW_STOCK_SC)
                    holder.caseQty.setVisibility(View.GONE);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.ref = position;
            holder.pname.setText(product.getProductName());
            holder.prodcode.setText(product.getProductCode() + "");

            holder.caseQty.setVisibility(View.GONE);
            holder.outerQty.setVisibility(View.GONE);

            int total = 0;
            if (product.getCsPiece() > 0)
                total = product.getCsPiece();
            if (product.getCsCase() > 0)
                total = total + (product.getCsCase() * product.getCasesize());
            if (product.getCsOuter() > 0)
                total = total + (product.getCsOuter() * product.getOutersize());

            holder.pieceQty.setText(total + "");

            return (row);
        }

        public ClosingStockReportBo getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }
    }

    class ViewHolder {
        private TextView pname, pieceQty, caseQty, outerQty, prodcode;
        int ref;
    }
}
