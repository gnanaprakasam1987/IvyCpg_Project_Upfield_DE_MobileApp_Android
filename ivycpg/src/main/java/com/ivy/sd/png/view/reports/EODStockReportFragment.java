package com.ivy.sd.png.view.reports;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.print.EODStockReportPreviewScreen;

import java.util.ArrayList;

@Deprecated
public class EODStockReportFragment extends Fragment {
    private BusinessModel bmodel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        bmodel.reportHelper.downloadEODReport();
        bmodel.reportHelper.updateBaseUOM("ORDER", 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_eod_stock,
                container, false);


        ListView lv = view.findViewById(R.id.list);
        Button btnPrint = view.findViewById(R.id.print);
        LinearLayout layoutPrint = view.findViewById(R.id.ll_explist);


        setUpViews(view);
        ArrayList<StockReportBO> mStockReportList = doDataLogic();

        MyAdapter adapter = new MyAdapter(mStockReportList);
        lv.setAdapter(adapter);

        if (bmodel.configurationMasterHelper.SHOW_BUTTON_PRINT01 && !mStockReportList.isEmpty()) {
            btnPrint.setVisibility(View.VISIBLE);
            layoutPrint.setVisibility(View.VISIBLE);
        }

        btnPrint.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EODStockReportPreviewScreen.class);
                startActivity(i);
            }
        });

        return view;
    }


    private ArrayList<StockReportBO> doDataLogic() {
        ArrayList<StockReportBO> mStockReportList = new ArrayList<>();

        for (StockReportBO stockReportBO : bmodel.reportHelper.getEODStockReport()) {
            // update list if any qty >0
            if (stockReportBO.getSih() > 0 || stockReportBO.getEmptyBottleQty() > 0 || stockReportBO.getFreeIssuedQty() > 0
                    || stockReportBO.getSoldQty() > 0 || stockReportBO.getReplacementQty() > 0 || stockReportBO.getReturnQty() > 0) {

                int vanloadQty = (stockReportBO.getSih() + stockReportBO.getSoldQty() + stockReportBO.getFreeIssuedQty() + stockReportBO.getReplacementQty())
                        - (stockReportBO.getReturnQty() + stockReportBO.getEmptyBottleQty());

                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    int rem_SIH = 0;
                    int rem_vanLoad = 0;
                    int rem_sold = 0;
                    int rem_freeIssued = 0;
                    int rem_empty = 0;
                    int rem_replacementyQty = 0;
                    int rem_returnQty = 0;
                    boolean isUomWiseSplitted = false;

                    if (stockReportBO.isBaseUomCaseWise() && stockReportBO.getCaseSize() != 0) {
                        isUomWiseSplitted = true;

                        stockReportBO.setSih_cs(stockReportBO.getSih() / stockReportBO.getCaseSize());
                        stockReportBO.setEmptyBottleQty_cs(stockReportBO.getEmptyBottleQty() / stockReportBO.getCaseSize());
                        stockReportBO.setFreeIssuedQty_cs(stockReportBO.getFreeIssuedQty() / stockReportBO.getCaseSize());
                        stockReportBO.setSoldQty_cs(stockReportBO.getSoldQty() / stockReportBO.getCaseSize());
                        stockReportBO.setVanLoadQty_cs(vanloadQty / stockReportBO.getCaseSize());
                        stockReportBO.setReplacementQty_cs(stockReportBO.getReplacementQty() / stockReportBO.getCaseSize());
                        stockReportBO.setReturnQty_cs(stockReportBO.getReturnQty() / stockReportBO.getCaseSize());


                        rem_SIH = stockReportBO.getSih() % stockReportBO.getCaseSize();
                        rem_empty = stockReportBO.getEmptyBottleQty() % stockReportBO.getCaseSize();
                        rem_freeIssued = stockReportBO.getFreeIssuedQty() % stockReportBO.getCaseSize();
                        rem_sold = stockReportBO.getSoldQty() % stockReportBO.getCaseSize();
                        rem_vanLoad = vanloadQty % stockReportBO.getCaseSize();
                        rem_replacementyQty = stockReportBO.getReplacementQty() % stockReportBO.getCaseSize();
                        rem_returnQty = stockReportBO.getReturnQty() % stockReportBO.getCaseSize();
                    }

                    if (stockReportBO.isBaseUomOuterWise() && stockReportBO.getOuterSize() != 0) {
                        if (isUomWiseSplitted) {
                            stockReportBO.setSih_ou(rem_SIH / stockReportBO.getOuterSize());
                            stockReportBO.setEmptyBottleQty_ou(rem_empty / stockReportBO.getOuterSize());
                            stockReportBO.setFreeIssuedQty_ou(rem_freeIssued / stockReportBO.getOuterSize());
                            stockReportBO.setSoldQty_ou(rem_sold / stockReportBO.getOuterSize());
                            stockReportBO.setVanLoadQty_ou(rem_vanLoad / stockReportBO.getOuterSize());
                            stockReportBO.setReplacemnetQty_ou(rem_replacementyQty / stockReportBO.getOuterSize());
                            stockReportBO.setReturnQty_ou(rem_returnQty / stockReportBO.getCaseSize());

                            rem_SIH = rem_SIH % stockReportBO.getOuterSize();
                            rem_empty = rem_empty % stockReportBO.getOuterSize();
                            rem_freeIssued = rem_freeIssued % stockReportBO.getOuterSize();
                            rem_sold = rem_sold % stockReportBO.getOuterSize();
                            rem_vanLoad = rem_vanLoad % stockReportBO.getOuterSize();
                            rem_replacementyQty = rem_replacementyQty % stockReportBO.getOuterSize();
                            rem_returnQty = rem_returnQty % stockReportBO.getOuterSize();
                        } else {
                            isUomWiseSplitted = true;
                            stockReportBO.setSih_ou(stockReportBO.getSih() / stockReportBO.getOuterSize());

                            stockReportBO.setEmptyBottleQty_ou(stockReportBO.getEmptyBottleQty() / stockReportBO.getOuterSize());

                            stockReportBO.setFreeIssuedQty_ou(stockReportBO.getFreeIssuedQty() / stockReportBO.getOuterSize());

                            stockReportBO.setSoldQty_ou(stockReportBO.getSoldQty() / stockReportBO.getOuterSize());

                            stockReportBO.setVanLoadQty_ou(vanloadQty / stockReportBO.getOuterSize());

                            stockReportBO.setReplacemnetQty_ou(stockReportBO.getReplacementQty() / stockReportBO.getOuterSize());

                            stockReportBO.setReturnQty_ou(stockReportBO.getReturnQty() / stockReportBO.getOuterSize());

                            rem_SIH = stockReportBO.getSih() % stockReportBO.getOuterSize();
                            rem_empty = stockReportBO.getEmptyBottleQty() % stockReportBO.getOuterSize();
                            rem_freeIssued = stockReportBO.getFreeIssuedQty() % stockReportBO.getOuterSize();
                            rem_sold = stockReportBO.getSoldQty() % stockReportBO.getOuterSize();
                            rem_vanLoad = vanloadQty % stockReportBO.getOuterSize();
                            rem_replacementyQty = stockReportBO.getReplacementQty() % stockReportBO.getOuterSize();
                            rem_returnQty = stockReportBO.getReturnQty() % stockReportBO.getOuterSize();
                        }
                    }

                    if (isUomWiseSplitted) {
                        stockReportBO.setSih_pc(rem_SIH);
                        stockReportBO.setEmptyBottleQty_pc(rem_empty);
                        stockReportBO.setFreeIssuedQty_pc(rem_freeIssued);
                        stockReportBO.setSoldQty_pc(rem_sold);
                        stockReportBO.setVanLoadQty_pc(rem_vanLoad);
                        stockReportBO.setReplacementQty_pc(rem_replacementyQty);
                        stockReportBO.setReturnQty_pc(rem_returnQty);
                    } else {
                        stockReportBO.setVanLoadQty_pc(vanloadQty);
                        stockReportBO.setSoldQty_pc(stockReportBO.getSoldQty());
                        stockReportBO.setFreeIssuedQty_pc(stockReportBO.getFreeIssuedQty());
                        stockReportBO.setSih_pc(stockReportBO.getSih());
                        stockReportBO.setEmptyBottleQty_pc(stockReportBO.getEmptyBottleQty());
                        stockReportBO.setReplacementQty_pc(stockReportBO.getReplacementQty());
                        stockReportBO.setReturnQty_pc(stockReportBO.getReturnQty());
                    }
                } else {
                    stockReportBO.setVanLoadQty(vanloadQty);
                    // Remaining objects already set
                }

                mStockReportList.add(stockReportBO);
            }
        }
        return mStockReportList;
    }


    private void setUpViews(View view) {

        if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
            view.findViewById(R.id.ll_replacement).setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
            view.findViewById(R.id.ll_empty).setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
            view.findViewById(R.id.ll_free_issued).setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS ||
                bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS ||
                bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {

            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS) {
                setUpEOD_SIH_PSViews(view, View.VISIBLE);
            } else
                setUpEOD_SIH_PSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS) {
                setUpEOD_SIH_CSViews(view, View.VISIBLE);
            } else
                setUpEOD_SIH_CSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {
                setUpEOD_SIH_OUViews(view, View.VISIBLE);
            } else
                setUpEOD_SIH_OUViews(view, View.GONE);


        } else if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {


            if (bmodel.configurationMasterHelper.SHOW_EOD_OP)
                setUpEOD_SIH_PSViews(view, View.VISIBLE);
            else
                setUpEOD_SIH_PSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.SHOW_EOD_OC)
                setUpEOD_SIH_CSViews(view, View.VISIBLE);
            else
                setUpEOD_SIH_CSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.SHOW_EOD_OO)
                setUpEOD_SIH_OUViews(view, View.VISIBLE);
            else
                setUpEOD_SIH_OUViews(view, View.GONE);


        } else {
            hideAllViews(view);
        }
    }

    private void hideAllViews(View view) {

        view.findViewById(R.id.loading_stock_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.loading_stock_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_return_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_return_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_sold_stock_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_sold_stock_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_free_issued_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_free_issued_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_replacement_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_replacement_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_sih_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_sih_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_empty_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_empty_ou_title).setVisibility(View.GONE);
    }


    private void setUpEOD_SIH_PSViews(View view, int visible) {
        view.findViewById(R.id.loading_stock_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_return_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_free_issued_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_replacement_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_sih_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_empty_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_pc_title).setVisibility(visible);
    }

    private void setUpEOD_SIH_CSViews(View view, int visible) {

        view.findViewById(R.id.loading_stock_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_return_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_free_issued_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_replacement_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_sih_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_empty_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_cs_title).setVisibility(visible);
    }

    private void setUpEOD_SIH_OUViews(View view, int visible) {

        view.findViewById(R.id.loading_stock_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_return_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_replacement_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_sih_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_empty_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_ou_title).setVisibility(visible);
    }


    class ViewHolder {
        StockReportBO mSKUBO;
        int position;
        TextView mPName, mProductCode;
        TextView mLoadStock;
        TextView mSoldStock;
        TextView mFreeIssued;
        TextView mSIH;
        TextView mEmpty;
        TextView mReplacementTV;
        TextView tv_return;
        TextView mLoadStock_cs;
        TextView mLoadStock_ou;
        TextView mSoldStock_cs;
        TextView mSoldStock_ou;
        TextView mFreeIssued_cs;
        TextView mFreeIssued_ou;
        TextView mSIH_cs;
        TextView mSIH_ou;
        TextView mEmpty_cs;
        TextView mEmpty_ou;
        TextView mReplacementTV_cs;
        TextView mReplacementTV_ou;
        TextView tv_return_cs;
        TextView tv_return_ou;
        TextView mBatchNum;
        RelativeLayout replacementRL;
        RelativeLayout emptyRL;
        RelativeLayout freeIssuedRL;
    }

    private class MyAdapter extends ArrayAdapter<StockReportBO> {
        private final ArrayList<StockReportBO> items;

        public MyAdapter(ArrayList<StockReportBO> items) {
            super(getActivity(), R.layout.row_report_eod_stock, items);
            this.items = items;
        }

        public StockReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());
                final ViewGroup nullParent = null;
                convertView = inflater.inflate(R.layout.row_report_eod_stock, nullParent);

                holder.mPName = convertView.findViewById(R.id.pname);
                holder.mPName.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.mProductCode = convertView.findViewById(R.id.product_code);
                holder.mLoadStock = convertView.findViewById(R.id.loadstock);
                holder.mLoadStock_cs = convertView.findViewById(R.id.loadstock_cs);
                holder.mLoadStock_ou = convertView.findViewById(R.id.loadstock_ou);

                holder.tv_return = convertView.findViewById(R.id.tv_return);
                holder.tv_return_cs = convertView.findViewById(R.id.tv_return_cs);
                holder.tv_return_ou = convertView.findViewById(R.id.tv_return_ou);

                holder.mSoldStock = convertView.findViewById(R.id.soldstock);
                holder.mSoldStock_cs = convertView.findViewById(R.id.soldstock_cs);
                holder.mSoldStock_ou = convertView.findViewById(R.id.soldstock_ou);

                holder.freeIssuedRL = convertView.findViewById(R.id.ll_freeissued);

                holder.mFreeIssued = convertView.findViewById(R.id.freeissued);
                holder.mFreeIssued_cs = convertView.findViewById(R.id.freeissued_cs);
                holder.mFreeIssued_ou = convertView.findViewById(R.id.freeissued_ou);

                holder.mSIH = convertView.findViewById(R.id.sih);
                holder.mSIH_cs = convertView.findViewById(R.id.sih_cs);
                holder.mSIH_ou = convertView.findViewById(R.id.sih_ou);

                holder.emptyRL = convertView.findViewById(R.id.ll_empty);
                holder.mEmpty = convertView.findViewById(R.id.empty);
                holder.mEmpty_cs = convertView.findViewById(R.id.empty_cs);
                holder.mEmpty_ou = convertView.findViewById(R.id.empty_ou);

                holder.replacementRL = convertView.findViewById(R.id.ll_replacement);
                holder.mReplacementTV = convertView.findViewById(R.id.replacement);
                holder.mReplacementTV_cs = convertView.findViewById(R.id.replacement_cs);
                holder.mReplacementTV_ou = convertView.findViewById(R.id.replacement_ou);


                holder.mBatchNum = convertView.findViewById(R.id.batchnumber);

                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    if (bmodel.configurationMasterHelper.SHOW_EOD_OC) {
                        setSHOW_EOD_OC(holder, View.VISIBLE);
                    } else {
                        setSHOW_EOD_OC(holder, View.GONE);
                    }
                    if (bmodel.configurationMasterHelper.SHOW_EOD_OP) {
                        setSHOW_EOD_OP(holder, View.VISIBLE);
                    } else {
                        setSHOW_EOD_OP(holder, View.GONE);
                    }
                    if (bmodel.configurationMasterHelper.SHOW_EOD_OO) {
                        setSHOW_EOD_OO(holder, View.VISIBLE);
                    } else {
                        setSHOW_EOD_OO(holder, View.GONE);
                    }

                } else {
                    hideAllViews(holder);

                }

                if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
                    holder.replacementRL.setVisibility(View.VISIBLE);
                else
                    holder.replacementRL.setVisibility(View.GONE);

                if (bmodel.configurationMasterHelper.SHOW_STOCK_RETURN)
                    convertView.findViewById(R.id.ll_return).setVisibility(View.VISIBLE);
                else
                    convertView.findViewById(R.id.ll_return).setVisibility(View.GONE);

                if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                    holder.emptyRL.setVisibility(View.VISIBLE);
                else
                    holder.emptyRL.setVisibility(View.GONE);
                if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                    holder.freeIssuedRL.setVisibility(View.VISIBLE);
                else
                    holder.freeIssuedRL.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.mProductCode.setVisibility(View.GONE);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mSKUBO = items.get(position);
            holder.position = position;

            holder.mPName.setText(holder.mSKUBO.getProductName());
            if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code)
                        + ": " + holder.mSKUBO.getProductCode() + " ";
                holder.mProductCode.setText(prodCode);
            }

            if (!holder.mSKUBO.getBatchNo().isEmpty()) {
                holder.mBatchNum.setVisibility(View.VISIBLE);
                String batchNo = getResources().getString(R.string.batch_no) + ": " +
                        holder.mSKUBO.getBatchNo();
                holder.mBatchNum.setText(batchNo);
            } else
                holder.mBatchNum.setVisibility(View.GONE);


            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS ||
                    bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS ||
                    bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {

                if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {
                    if (holder.mSKUBO.getOuterSize() != 0) {
                        holder.mLoadStock.setText(String.valueOf(SDUtil.mathRoundoff((double) holder.mSKUBO.getVanLoadQty() / holder.mSKUBO.getOuterSize())));
                        holder.mSoldStock.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getSoldQty() / holder.mSKUBO.getOuterSize()))));
                        holder.mFreeIssued.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getFreeIssuedQty() / holder.mSKUBO.getOuterSize()))));
                        holder.mSIH.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getSih() / holder.mSKUBO.getOuterSize()))));
                        holder.mEmpty.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getEmptyBottleQty() / holder.mSKUBO.getOuterSize()))));
                        holder.mReplacementTV.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getReplacementQty() / holder.mSKUBO.getOuterSize()))));
                        holder.tv_return.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getReturnQty() / holder.mSKUBO.getOuterSize()))));
                    } else {
                        holder.mLoadStock.setText(String.valueOf(holder.mSKUBO.getVanLoadQty()));
                        holder.mSoldStock.setText(String.valueOf(holder.mSKUBO.getSoldQty()));
                        holder.mFreeIssued.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty()));
                        holder.mSIH.setText(String.valueOf(holder.mSKUBO.getSih()));
                        holder.mEmpty.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty()));
                        holder.mReplacementTV.setText(String.valueOf(holder.mSKUBO.getReplacementQty()));
                        holder.tv_return.setText(String.valueOf(holder.mSKUBO.getReturnQty()));
                    }
                } else if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS) {
                    if (holder.mSKUBO.getCaseSize() != 0) {
                        holder.mLoadStock.setText(String.valueOf(SDUtil.mathRoundoff((double) holder.mSKUBO.getVanLoadQty() / holder.mSKUBO.getCaseSize())));
                        holder.mSoldStock.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getSoldQty() / holder.mSKUBO.getCaseSize()))));
                        holder.mFreeIssued.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getFreeIssuedQty() / holder.mSKUBO.getCaseSize()))));
                        holder.mSIH.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getSih() / holder.mSKUBO.getCaseSize()))));
                        holder.mEmpty.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getEmptyBottleQty() / holder.mSKUBO.getCaseSize()))));
                        holder.mReplacementTV.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getReplacementQty() / holder.mSKUBO.getCaseSize()))));
                        holder.tv_return.setText(String.valueOf(SDUtil.mathRoundoff(((double) holder.mSKUBO.getReturnQty() / holder.mSKUBO.getCaseSize()))));
                    } else {
                        holder.mLoadStock.setText(String.valueOf(holder.mSKUBO.getVanLoadQty()));
                        holder.mSoldStock.setText(String.valueOf(holder.mSKUBO.getSoldQty()));
                        holder.mFreeIssued.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty()));
                        holder.mSIH.setText(String.valueOf(holder.mSKUBO.getSih()));
                        holder.mEmpty.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty()));
                        holder.mReplacementTV.setText(String.valueOf(holder.mSKUBO.getReplacementQty()));
                        holder.tv_return.setText(String.valueOf(holder.mSKUBO.getReturnQty()));
                    }

                } else //if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS) {

                    holder.mLoadStock.setText(String.valueOf(holder.mSKUBO.getVanLoadQty()));
                holder.mSoldStock.setText(String.valueOf(holder.mSKUBO.getSoldQty()));
                holder.mFreeIssued.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty()));
                holder.mSIH.setText(String.valueOf(holder.mSKUBO.getSih()));
                holder.mEmpty.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty()));
                holder.mReplacementTV.setText(String.valueOf(holder.mSKUBO.getReplacementQty()));
                holder.tv_return.setText(String.valueOf(holder.mSKUBO.getReturnQty()));
                // }
            } else if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {

                holder.mSIH_cs.setText(String.valueOf(holder.mSKUBO.getSih_cs()));
                holder.mEmpty_cs.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty_cs()));
                holder.mFreeIssued_cs.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty_cs()));
                holder.mSoldStock_cs.setText(String.valueOf(holder.mSKUBO.getSoldQty_cs()));
                holder.mLoadStock_cs.setText(String.valueOf(holder.mSKUBO.getVanLoadQty_cs()));
                holder.mReplacementTV_cs.setText(String.valueOf(holder.mSKUBO.getReplacementQty_cs()));
                holder.tv_return_cs.setText(String.valueOf(holder.mSKUBO.getReturnQty_cs()));

                holder.mSIH_ou.setText(String.valueOf(holder.mSKUBO.getSih_ou()));
                holder.mEmpty_ou.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty_ou()));
                holder.mFreeIssued_ou.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty_ou()));
                holder.mSoldStock_ou.setText(String.valueOf(holder.mSKUBO.getSoldQty_ou()));
                holder.mLoadStock_ou.setText(String.valueOf(holder.mSKUBO.getVanLoadQty_ou()));
                holder.mReplacementTV_ou.setText(String.valueOf(holder.mSKUBO.getReplacemnetQty_ou()));
                holder.tv_return_ou.setText(String.valueOf(holder.mSKUBO.getReturnQty_ou()));


                holder.mSIH.setText(String.valueOf(holder.mSKUBO.getSih_pc()));
                holder.mEmpty.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty_pc()));
                holder.mFreeIssued.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty_pc()));
                holder.mSoldStock.setText(String.valueOf(holder.mSKUBO.getSoldQty_pc()));
                holder.mLoadStock.setText(String.valueOf(holder.mSKUBO.getVanLoadQty_pc()));
                holder.mReplacementTV.setText(String.valueOf(holder.mSKUBO.getReplacementQty_pc()));
                holder.tv_return.setText(String.valueOf(holder.mSKUBO.getReturnQty_pc()));

            } else {
                holder.mLoadStock.setText(String.valueOf(holder.mSKUBO.getVanLoadQty()));
                holder.mSoldStock.setText(String.valueOf(holder.mSKUBO.getSoldQty()));
                holder.mFreeIssued.setText(String.valueOf(holder.mSKUBO.getFreeIssuedQty()));
                holder.mSIH.setText(String.valueOf(holder.mSKUBO.getSih()));
                holder.mEmpty.setText(String.valueOf(holder.mSKUBO.getEmptyBottleQty()));
                holder.mReplacementTV.setText(String.valueOf(holder.mSKUBO.getReplacementQty()));
                holder.tv_return.setText(String.valueOf(holder.mSKUBO.getReturnQty()));
            }

            return convertView;
        }

        private void hideAllViews(ViewHolder holder) {

            holder.mSoldStock_cs.setVisibility(View.GONE);
            holder.mSoldStock_ou.setVisibility(View.GONE);

            holder.mLoadStock_cs.setVisibility(View.GONE);
            holder.mLoadStock_ou.setVisibility(View.GONE);

            holder.tv_return_cs.setVisibility(View.GONE);
            holder.tv_return_ou.setVisibility(View.GONE);

            holder.mFreeIssued_cs.setVisibility(View.GONE);
            holder.mFreeIssued_ou.setVisibility(View.GONE);

            holder.mSIH_cs.setVisibility(View.GONE);
            holder.mSIH_ou.setVisibility(View.GONE);

            holder.mEmpty_cs.setVisibility(View.GONE);
            holder.mEmpty_ou.setVisibility(View.GONE);

            holder.mReplacementTV_cs.setVisibility(View.GONE);
            holder.mReplacementTV_ou.setVisibility(View.GONE);
        }

        private void setSHOW_EOD_OC(ViewHolder holder, int visible) {
            holder.mSoldStock_cs.setVisibility(visible);
            holder.mLoadStock_cs.setVisibility(visible);
            holder.tv_return_cs.setVisibility(visible);
            holder.mFreeIssued_cs.setVisibility(visible);
            holder.mSIH_cs.setVisibility(visible);
            holder.mEmpty_cs.setVisibility(visible);
            holder.mReplacementTV_cs.setVisibility(visible);
        }

        private void setSHOW_EOD_OP(ViewHolder holder, int visible) {
            holder.mSoldStock.setVisibility(visible);
            holder.mLoadStock.setVisibility(visible);
            holder.tv_return.setVisibility(visible);
            holder.mFreeIssued.setVisibility(visible);
            holder.mSIH.setVisibility(visible);
            holder.mEmpty.setVisibility(visible);
            holder.mReplacementTV.setVisibility(visible);

        }

        private void setSHOW_EOD_OO(ViewHolder holder, int visible) {
            holder.mSoldStock_ou.setVisibility(visible);
            holder.mLoadStock_ou.setVisibility(visible);
            holder.tv_return_ou.setVisibility(visible);
            holder.mFreeIssued_ou.setVisibility(visible);
            holder.mSIH_ou.setVisibility(visible);
            holder.mEmpty_ou.setVisibility(visible);
            holder.mReplacementTV_ou.setVisibility(visible);

        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        bmodel = null;
    }
}
