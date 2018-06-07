package com.ivy.sd.png.view.reports.eodstockreport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;


public class EodStockAdapter extends ArrayAdapter<StockReportBO> {
    private final ArrayList<StockReportBO> items;
    private BusinessModel bmodel;

    public EodStockAdapter(ArrayList<StockReportBO> items, BusinessModel businessModel, Context context) {
        super(context, R.layout.row_report_eod_stock, items);
        this.items = items;
        this.bmodel = businessModel;
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

            final ViewGroup nullParent = null;
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report_eod_stock, nullParent);

            holder.mPName = convertView.findViewById(R.id.pname);
            holder.mPName.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

            holder.mLoadStock = convertView.findViewById(R.id.loadstock);
            holder.mLoadStock_cs = convertView.findViewById(R.id.loadstock_cs);
            holder.mLoadStock_ou = convertView.findViewById(R.id.loadstock_ou);

            holder.tv_return = convertView.findViewById(R.id.tv_return);
            holder.tv_return_cs = convertView.findViewById(R.id.tv_return_cs);
            holder.tv_return_ou = convertView.findViewById(R.id.tv_return_ou);

            holder.mSoldStock = convertView.findViewById(R.id.soldstock);
            holder.mSoldStock_cs = convertView.findViewById(R.id.soldstock_cs);
            holder.mSoldStock_ou = convertView.findViewById(R.id.soldstock_ou);

            holder.freeIssuedRL = convertView.findViewById(R.id.rl_freeissued);

            holder.mFreeIssued = convertView.findViewById(R.id.freeissued);
            holder.mFreeIssued_cs = convertView.findViewById(R.id.freeissued_cs);
            holder.mFreeIssued_ou = convertView.findViewById(R.id.freeissued_ou);

            holder.mSIH = convertView.findViewById(R.id.sih);
            holder.mSIH_cs = convertView.findViewById(R.id.sih_cs);
            holder.mSIH_ou = convertView.findViewById(R.id.sih_ou);

            holder.emptyRL = convertView.findViewById(R.id.rl_empty);
            holder.mEmpty = convertView.findViewById(R.id.empty);
            holder.mEmpty_cs = convertView.findViewById(R.id.empty_cs);
            holder.mEmpty_ou = convertView.findViewById(R.id.empty_ou);

            holder.replacementRL = convertView.findViewById(R.id.rl_replacement);
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
            if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                holder.emptyRL.setVisibility(View.VISIBLE);
            else
                holder.emptyRL.setVisibility(View.GONE);
            if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                holder.freeIssuedRL.setVisibility(View.VISIBLE);
            else
                holder.freeIssuedRL.setVisibility(View.GONE);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mSKUBO = items.get(position);
        holder.position = position;

        holder.mPName.setText(holder.mSKUBO.getProductName());
        if (holder.mSKUBO.getBatchNo() != null) {
            holder.mBatchNum.setVisibility(View.VISIBLE);
            holder.mBatchNum.setText(holder.mSKUBO.getBatchNo());
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


    class ViewHolder {
        StockReportBO mSKUBO;
        int position;
        TextView mPName;
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
}
