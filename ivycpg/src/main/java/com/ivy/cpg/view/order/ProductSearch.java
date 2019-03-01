package com.ivy.cpg.view.order;

import android.content.Context;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class ProductSearch implements TextView.OnEditorActionListener {

    private Context context;
    private EditText mEdt_searchproductName;
    private Vector<ProductMasterBO> masterList,searchedList;
    private BusinessModel bModel;
    private int loadStockedProduct=-1;

    public ProductSearch(Context context){
        this.context=context;
        mEdt_searchproductName = (EditText) context.findViewById(R.id.edt_searchproductName);
        mEdt_searchproductName.setOnEditorActionListener(this);

        if (bModel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY) {
            if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                loadStockedProduct = bModel.getRetailerMasterBO().getIsVansales() == 1 ? 1 : 0;
            } else {
                loadStockedProduct = bModel.configurationMasterHelper.IS_INVOICE ? 1 : 0;
            }
        }
    }

    public void setMasterList(Vector<ProductMasterBO> masterList){
        this.masterList=masterList;
    }

    @Override
    public boolean onEditorAction(TextView textView, int arg1, KeyEvent keyEvent) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (mEdt_searchproductName.getText().length() >= 3) {
                searchAsync = new SearchAsync();
                searchAsync.execute();
            } else {
                Toast.makeText(context, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private class SearchAsync extends
            AsyncTask<Integer, Integer, Boolean> {


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            loadSearchedList();

            return true;
        }

        protected void onPostExecute(Boolean result) {


        }
    }

    private void loadSearchedList(String textToFilter) {
        try {
            Vector<ProductMasterBO> items = masterList;
            int siz = items.size();
            searchedList = new Vector<>();

            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);

                if (bModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !OrderHelper.getInstance(context).isQuickCall &&
                        !ret.getParentHierarchy().contains("/" + bModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;

                if (bModel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                    continue;

                if (loadStockedProduct == -1
                        || (loadStockedProduct == 1 ? ret.getSIH() > 0 : ret.getWSIH() > 0)) {

                    if (!bModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || (bModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER && ret.getIndicativeOrder_oc() > 0)) {


                            if (ret.getBarCode() != null  && (ret.getBarCode().toLowerCase().contains(textToFilter.toLowerCase())
                                    || ret.getCasebarcode().toLowerCase().contains(textToFilter.toLowerCase())
                                    || ret.getOuterbarcode().toLowerCase(). contains(textToFilter.toLowerCase()))
                                    && ret.getIsSaleable() == 1) {

                                    if (bModel.configurationMasterHelper.IS_QTY_INCREASE) {
                                        if (mEdt_searchproductName.getText().toString().equals(ret.getBarCode())) {
                                            ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                        } else if (mEdt_searchproductName.getText().toString().equals(ret.getCasebarcode())) {
                                            ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                        } else if (mEdt_searchproductName.getText().toString().equals(ret.getOuterbarcode())) {
                                            ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                        }
                                    }
                                    mylist.add(ret);

                            }
                            if (((ret.getRField1() != null && ret.getRField1().toLowerCase().contains(textToFilter.toLowerCase()))
                                    || (ret.getProductCode() != null && ret.getProductCode().toLowerCase().contains(textToFilter.toLowerCase()))) && ret.getIsSaleable() == 1) {
                                    mylist.add(ret);
                            }
                            if (ret.getProductShortName() != null && ret.getProductShortName()
                                    .toLowerCase()
                                    .contains(textToFilter) && ret.getIsSaleable() == 1)
                                    mylist.add(ret);

                    }
                }
            }


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
