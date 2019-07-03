package com.ivy.cpg.view.stockcheck;

import com.ivy.sd.png.bo.ProductMasterBO;

import java.util.ArrayList;

/**
 * Created by dharmapriya.k on 7/12/17.
 */

public interface StockCheckContractor {
    interface StockCheckPresenter {
        void setView(StockCheckView stockCheckView);

        void loadInitialData();
    }

    interface StockCheckView {
        void showProgressDialog();

        void dismissAlertDialog();

        void showStockSavedDialog();

        void showAlert();

        void updateListFromFilter(ArrayList<ProductMasterBO> stockList);

        void showSearchValidationToast();

        void scrollToSelectedTabPosition();

        void savePromptMessage(int type, String text);

        void notifyListChange();
    }
}
