package com.ivy.cpg.view.reports.eodstockreport;


import android.content.Context;

import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import javax.inject.Inject;

public class EodStockModel implements IEodStockModelPresenter {
    private IEodStockView mEodStockView;
    private Context mContext;
    private BusinessModel mBusinessModel;

    @Inject
    public EodReportHelper eodReportHelper;

    public EodStockModel(Context context, IEodStockView iEodStockView) {
        this.mEodStockView = iEodStockView;
        this.mContext = context;
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();

        EodReportComponent eodReportComponent = DaggerEodReportComponent.builder().eodStockReportModule
                (new EodStockReportModule(mBusinessModel)).build();
        eodReportComponent.inject(this);
    }

    private ArrayList<StockReportBO> doDataLogic() {
        ArrayList<StockReportBO> mStockReportList = new ArrayList<>();

        for (StockReportBO stockReportBO : eodReportHelper.getEODStockReport()) {
            // update list if any qty >0
            if (stockReportBO.getSih() > 0 || stockReportBO.getEmptyBottleQty() > 0 || stockReportBO.getFreeIssuedQty() > 0
                    || stockReportBO.getSoldQty() > 0 || stockReportBO.getReplacementQty() > 0 || stockReportBO.getReturnQty() > 0
                    || stockReportBO.getVanUnloadQty() > 0 || stockReportBO.getNonSalableQty() > 0) {

                int vanloadQty = (stockReportBO.getSih() +
                        stockReportBO.getSoldQty() +
                        stockReportBO.getFreeIssuedQty() +
                        stockReportBO.getReplacementQty() + stockReportBO.getVanUnloadQty())
                        - (stockReportBO.getReturnQty() + stockReportBO.getEmptyBottleQty());

                if (mBusinessModel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    int rem_SIH = 0;
                    int rem_vanLoad = 0;
                    int rem_sold = 0;
                    int rem_freeIssued = 0;
                    int rem_empty = 0;
                    int rem_replacementyQty = 0;
                    int rem_returnQty = 0;
                    boolean isUomWiseSplitted = false;
                    int rem_nonSalableQty = 0;
                    int rem_vanUnloadQty = 0;


                    if (stockReportBO.isBaseUomCaseWise() && stockReportBO.getCaseSize() != 0) {
                        isUomWiseSplitted = true;

                        stockReportBO.setSih_cs(stockReportBO.getSih() / stockReportBO.getCaseSize());
                        stockReportBO.setEmptyBottleQty_cs(stockReportBO.getEmptyBottleQty() / stockReportBO.getCaseSize());
                        stockReportBO.setFreeIssuedQty_cs(stockReportBO.getFreeIssuedQty() / stockReportBO.getCaseSize());
                        stockReportBO.setSoldQty_cs(stockReportBO.getSoldQty() / stockReportBO.getCaseSize());
                        stockReportBO.setVanLoadQty_cs(vanloadQty / stockReportBO.getCaseSize());
                        stockReportBO.setReplacementQty_cs(stockReportBO.getReplacementQty() / stockReportBO.getCaseSize());
                        stockReportBO.setReturnQty_cs(stockReportBO.getReturnQty() / stockReportBO.getCaseSize());
                        stockReportBO.setNonsalableQty_cs(stockReportBO.getNonSalableQty() / stockReportBO.getCaseSize());
                        stockReportBO.setVanUnloadQty_cs(stockReportBO.getVanUnloadQty() / stockReportBO.getCaseSize());

                        rem_SIH = stockReportBO.getSih() % stockReportBO.getCaseSize();
                        rem_empty = stockReportBO.getEmptyBottleQty() % stockReportBO.getCaseSize();
                        rem_freeIssued = stockReportBO.getFreeIssuedQty() % stockReportBO.getCaseSize();
                        rem_sold = stockReportBO.getSoldQty() % stockReportBO.getCaseSize();
                        rem_vanLoad = vanloadQty % stockReportBO.getCaseSize();
                        rem_replacementyQty = stockReportBO.getReplacementQty() % stockReportBO.getCaseSize();
                        rem_returnQty = stockReportBO.getReturnQty() % stockReportBO.getCaseSize();
                        rem_nonSalableQty = stockReportBO.getNonSalableQty() % stockReportBO.getCaseSize();
                        rem_vanUnloadQty = stockReportBO.getVanUnloadQty() % stockReportBO.getCaseSize();
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
                            stockReportBO.setNonsalableQty_ou(rem_nonSalableQty / stockReportBO.getOuterSize());
                            stockReportBO.setVanUnloadQty_ou(rem_vanUnloadQty / stockReportBO.getOuterSize());

                            rem_SIH = rem_SIH % stockReportBO.getOuterSize();
                            rem_empty = rem_empty % stockReportBO.getOuterSize();
                            rem_freeIssued = rem_freeIssued % stockReportBO.getOuterSize();
                            rem_sold = rem_sold % stockReportBO.getOuterSize();
                            rem_vanLoad = rem_vanLoad % stockReportBO.getOuterSize();
                            rem_replacementyQty = rem_replacementyQty % stockReportBO.getOuterSize();
                            rem_returnQty = rem_returnQty % stockReportBO.getOuterSize();
                            rem_nonSalableQty = rem_nonSalableQty % stockReportBO.getOuterSize();
                            rem_vanUnloadQty = rem_vanUnloadQty % stockReportBO.getOuterSize();
                        } else {
                            isUomWiseSplitted = true;
                            stockReportBO.setSih_ou(stockReportBO.getSih() / stockReportBO.getOuterSize());

                            stockReportBO.setEmptyBottleQty_ou(stockReportBO.getEmptyBottleQty() / stockReportBO.getOuterSize());

                            stockReportBO.setFreeIssuedQty_ou(stockReportBO.getFreeIssuedQty() / stockReportBO.getOuterSize());

                            stockReportBO.setSoldQty_ou(stockReportBO.getSoldQty() / stockReportBO.getOuterSize());

                            stockReportBO.setVanLoadQty_ou(vanloadQty / stockReportBO.getOuterSize());

                            stockReportBO.setReplacemnetQty_ou(stockReportBO.getReplacementQty() / stockReportBO.getOuterSize());

                            stockReportBO.setReturnQty_ou(stockReportBO.getReturnQty() / stockReportBO.getOuterSize());
                            stockReportBO.setNonsalableQty_ou(stockReportBO.getNonSalableQty() / stockReportBO.getOuterSize());
                            stockReportBO.setVanUnloadQty_ou((stockReportBO.getVanUnloadQty() / stockReportBO.getOuterSize()));


                            rem_SIH = stockReportBO.getSih() % stockReportBO.getOuterSize();
                            rem_empty = stockReportBO.getEmptyBottleQty() % stockReportBO.getOuterSize();
                            rem_freeIssued = stockReportBO.getFreeIssuedQty() % stockReportBO.getOuterSize();
                            rem_sold = stockReportBO.getSoldQty() % stockReportBO.getOuterSize();
                            rem_vanLoad = vanloadQty % stockReportBO.getOuterSize();
                            rem_replacementyQty = stockReportBO.getReplacementQty() % stockReportBO.getOuterSize();
                            rem_returnQty = stockReportBO.getReturnQty() % stockReportBO.getOuterSize();
                            rem_nonSalableQty = rem_nonSalableQty % stockReportBO.getOuterSize();
                            rem_vanUnloadQty = rem_vanUnloadQty % stockReportBO.getOuterSize();
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
                        stockReportBO.setNonsalableQty_pc(rem_nonSalableQty);
                        stockReportBO.setVanUnloadQty_pc(rem_vanUnloadQty);
                    } else {
                        stockReportBO.setVanLoadQty_pc(vanloadQty);
                        stockReportBO.setSoldQty_pc(stockReportBO.getSoldQty());
                        stockReportBO.setFreeIssuedQty_pc(stockReportBO.getFreeIssuedQty());
                        stockReportBO.setSih_pc(stockReportBO.getSih());
                        stockReportBO.setEmptyBottleQty_pc(stockReportBO.getEmptyBottleQty());
                        stockReportBO.setReplacementQty_pc(stockReportBO.getReplacementQty());
                        stockReportBO.setReturnQty_pc(stockReportBO.getReturnQty());
                        stockReportBO.setNonsalableQty_pc(stockReportBO.getNonSalableQty());
                        stockReportBO.setVanUnloadQty_pc(stockReportBO.getVanUnloadQty());
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

    @Override
    public void setAdapter() {
        ArrayList<StockReportBO> mStockReportList = doDataLogic();
        EodStockAdapter adapter = new EodStockAdapter(mStockReportList, mBusinessModel, mContext);
        mEodStockView.setAdapter(adapter);
    }

    @Override
    public void downloadEodReport() {
        eodReportHelper.downloadEODReport();
        eodReportHelper.updateBaseUOM("ORDER", 1);
    }
}
