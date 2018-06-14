package com.ivy.cpg.view.stockcheck;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 7/12/17.
 */

public class StockCheckPresenterImpl implements StockCheckContractor.StockCheckPresenter {
    private Context context;
    private BusinessModel businessModel;
    private PriceTrackingHelper priceTrackingHelper;
    private StockCheckContractor.StockCheckView stockCheckView;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayList<String> fiveFilter_productIDs;
    public final String GENERAL = "General";
    public int mSelectedLocationIndex;
    public String generalButton;
    private String brandButton;
    public HashMap<Integer, Integer> mCompetitorSelectedIdByLevelId;
    private Vector<LevelBO> parentidList;
    private String filtertext;
    public HashMap<Integer, Integer> mSelectedIdByLevelId;
    public ArrayList<String> mSearchTypeArray = new ArrayList<>();
    public boolean remarks_button_enable = true;
    public boolean isSpecialFilter_enabled = true;
    public String strBarCodeSearch = "ALL";
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;

    StockCheckPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        priceTrackingHelper = PriceTrackingHelper.getInstance(context);
    }

    @Override
    public void setView(StockCheckContractor.StockCheckView stockCheckView) {
        this.stockCheckView = stockCheckView;
    }

    public void loadInitialData() {
        prepareAdapters();
    }

    public HashMap<String, String> getSelectedFilterMap() {
        return mSelectedFilterMap;
    }

    public void putValueToFilterMap(String value) {
        if (value.equals("")) {
            value = GENERAL;
        }
        mSelectedFilterMap.put("General", value);
    }


    public void saveClosingStock(ArrayList<ProductMasterBO> stockList) {
        if (businessModel.hasStockCheck()) {
            if (!businessModel.configurationMasterHelper.IS_REASON_FOR_ALL_NON_STOCK_PRODUCTS || businessModel.isReasonSelectedForAllProducts()) {
                new SaveClosingStockAsyncTask(stockList).execute();
            } else {
                String text = " ";
                for (ConfigureBO configureBO : getGeneralFilter()) {


                    if (configureBO.getConfigCode().equalsIgnoreCase("Filt11")) {
                        if (text.length() > 1)
                            text += ",";
                        text += configureBO.getMenuName();
                    } else if (configureBO.getConfigCode().equalsIgnoreCase("Filt12")) {
                        if (text.length() > 1)
                            text += ",";
                        text += configureBO.getMenuName();
                    }
                }
                stockCheckView.savePromptMessage(1, text);
            }
        } else {
            stockCheckView.savePromptMessage(0, "");
        }

    }

    public Vector<ConfigureBO> getGeneralFilter() {
        return businessModel.configurationMasterHelper.getGenFilter();
    }

    public String getDefaultFilter() {
        String defaultFilter = "";
        try {
            Vector<ConfigureBO> genfilter = getGeneralFilter();
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getHasLink() == 1) {
                    if (!businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                        defaultFilter = genfilter.get(i).getConfigCode();
                        break;
                    } else {
                        if (businessModel.getRetailerMasterBO().getIsVansales() == 1) {
                            if (genfilter.get(i).getConfigCode().equals("Filt13")) {
                                defaultFilter = genfilter.get(i).getConfigCode();
                                break;
                            } else if (!genfilter.get(i).getConfigCode().equals("Filt08")) {
                                defaultFilter = genfilter.get(i).getConfigCode();
                                break;
                            }
                        } else {
                            if (genfilter.get(i).getConfigCode().equals("Filt08")) {
                                defaultFilter = genfilter.get(i).getConfigCode();
                                break;
                            } else if (!genfilter.get(i).getConfigCode().equals("Filt13")) {
                                defaultFilter = genfilter.get(i).getConfigCode();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return defaultFilter;
    }

    /**
     * Save the values in Async task through Background
     *
     * @author gnanaprakasam.d
     */
    class SaveClosingStockAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private ArrayList<ProductMasterBO> stockList;

        SaveClosingStockAsyncTask(ArrayList<ProductMasterBO> stockList) {
            this.stockList = stockList;
        }


        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                // save price check
                if (businessModel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                    priceTrackingHelper.savePriceTransaction(context.getApplicationContext(), stockList);

                // save near expiry
                businessModel.saveNearExpiry();

                // Save closing stock
                businessModel.saveClosingStock(false);
                // update review plan in DB
                businessModel.setReviewPlanInDB();
                businessModel.saveModuleCompletion(HomeScreenTwo.MENU_STOCK);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e + "");
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPreExecute() {
            stockCheckView.showProgressDialog();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            stockCheckView.dismissAlertDialog();
            if (result == Boolean.TRUE) {
                businessModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

                stockCheckView.showStockSavedDialog();
            }
        }
    }

    public void getFilteredList(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId,
                                ArrayList<Integer> mAttributeProducts, String mFilterText) {
        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();
        fiveFilter_productIDs = new ArrayList<>();
        brandButton = filtertext;
        this.parentidList = mParentIdList;
        this.filtertext = mFilterText;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        if (items == null) {
            stockCheckView.showAlert();
            return;
        }
        if (mSelectedIdByLevelId != null && businessModel.isMapEmpty(mSelectedIdByLevelId) == false) {
            mCompetitorSelectedIdByLevelId = new HashMap<>();
        }
        ArrayList<ProductMasterBO> stockList = new ArrayList<>();
        //
        if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {// Only own products
            if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if (levelBO.getProductID() == sku.getParentid()) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                                if (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID()))) {
                                    stockList.add(sku);
                                    fiveFilter_productIDs.add(sku.getProductID());
                                }
                        }
                    }
                }
            } else if (mAttributeProducts == null && !parentidList.isEmpty()) {// product filter alone selected
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if (levelBO.getProductID() == sku.getParentid()) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                                stockList.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else if (mAttributeProducts != null && !parentidList.isEmpty()) {// Attribute filter alone selected
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (pid == SDUtil.convertToInt(sku.getProductID())) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                                stockList.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else {
                for (ProductMasterBO sku : items) {
                    if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                        stockList.add(sku);
                    fiveFilter_productIDs.add(sku.getProductID());
                }
            }
        } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {// Only competitor products
            if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if (levelBO.getProductID() == sku.getParentid()) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                                if (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID()))) {
                                    stockList.add(sku);
                                    fiveFilter_productIDs.add(sku.getProductID());
                                }
                        }
                    }
                }
            } else if (mAttributeProducts == null && !parentidList.isEmpty()) {// product filter alone selected
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if (levelBO.getProductID() == sku.getParentid()) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                                stockList.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else if (mAttributeProducts != null && !parentidList.isEmpty()) {// Attribute filter alone selected
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (pid == SDUtil.convertToInt(sku.getProductID())) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                                stockList.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else {
                for (ProductMasterBO sku : items) {
                    if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                        stockList.add(sku);
                    fiveFilter_productIDs.add(sku.getProductID());
                }
            }
        } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {//Both Own and Competitor products
            if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if (levelBO.getProductID() == sku.getParentid()) {
                            if (sku.getIsSaleable() == 1)
                                if (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID()))) {
                                    stockList.add(sku);
                                    fiveFilter_productIDs.add(sku.getProductID());
                                }
                        }
                    }
                }
            } else if (mAttributeProducts == null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {// product filter alone selected
                    for (ProductMasterBO sku : items) {
                        if (levelBO.getProductID() == sku.getParentid()) {
                            if (sku.getIsSaleable() == 1)
                                stockList.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else if (mAttributeProducts != null && !parentidList.isEmpty()) {
                for (int pid : mAttributeProducts) {// Attribute filter alone selected
                    for (ProductMasterBO sku : items) {
                        if (pid == SDUtil.convertToInt(sku.getProductID())) {
                            if (sku.getIsSaleable() == 1)
                                stockList.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else {
                for (ProductMasterBO sku : items) {
                    if (sku.getIsSaleable() == 1)
                        stockList.add(sku);
                    fiveFilter_productIDs.add(sku.getProductID());

                }
            }
        }

        if (businessModel.configurationMasterHelper.SHOW_SPL_FILTER) {
            Vector<ProductMasterBO> temp = new Vector<>();
            String generalTxt = generalButton;
            for (ProductMasterBO ret : stockList) {
                if (isSpecialFilterAppliedProduct(generalTxt, ret))
                    temp.add(ret);
            }
            stockList.clear();
            stockList.addAll(temp);
        }


        stockCheckView.updateListFromFilter(stockList);
    }

    private boolean isSpecialFilterAppliedProduct(String generaltxt, ProductMasterBO ret) {
        final String mCommon = "Filt01";
        final String mSbd = "Filt02";
        final String mSbdGaps = "Filt03";
        final String mOrdered = "Filt04";
        final String mPurchased = "Filt05";
        final String mInitiative = "Filt06";
        final String mOnAllocation = "Filt07";
        final String mInStock = "Filt08";
        final String mPromo = "Filt09";
        final String mMustSell = "Filt10";
        final String mFocusBrand = "Filt11";
        final String mFocusBrand2 = "Filt12";
        final String msih = "Filt13";
        final String mOOS = "Filt14";
        final String mNMustSell = "Filt16";
        final String mNearExpiryTag = "Filt19";
        final String mFocusBrand3 = "Filt20";
        final String mFocusBrand4 = "Filt21";
        final String mSMP = "Filt22";
        final String mCompertior = "Filt23";
        final String mShelf = "Filt24";

        return generaltxt.equalsIgnoreCase(mSbd) && ret.isRPS()
                || (generaltxt.equalsIgnoreCase(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (generaltxt.equalsIgnoreCase(mPurchased) && ret.getIsPurchased() == 1)
                || (generaltxt.equalsIgnoreCase(mInitiative) && ret.getIsInitiativeProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mCommon) && (ret.isRPS() || (ret.getIsInitiativeProduct() == 1) || (ret.getIsPurchased() == 1)))
                || (generaltxt.equalsIgnoreCase(mSbdGaps) && (ret.isRPS() && !ret.isSBDAcheived()))
                || (generaltxt.equalsIgnoreCase(GENERAL))
                || (generaltxt.equalsIgnoreCase(mInStock) && ret.getWSIH() > 0)
                || (generaltxt.equalsIgnoreCase(mOnAllocation) && ret.getSIH() > 0 && ret.isAllocation() == 1 && businessModel.configurationMasterHelper.IS_SIH_VALIDATION)
                || (generaltxt.equalsIgnoreCase(mPromo) && ret.isPromo())
                || (generaltxt.equalsIgnoreCase(mMustSell) && ret.getIsMustSell() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand) && ret.getIsFocusBrand() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand2) && ret.getIsFocusBrand2() == 1)
                || (generaltxt.equalsIgnoreCase(msih) && ret.getSIH() > 0)
                || (generaltxt.equalsIgnoreCase(mOOS) && ret.getOos() == 0)
                || (generaltxt.equalsIgnoreCase(mNMustSell) && ret.getIsNMustSell() == 1)
                || (generaltxt.equalsIgnoreCase(mNearExpiryTag) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand3) && ret.getIsFocusBrand3() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand4) && ret.getIsFocusBrand4() == 1)
                || (generaltxt.equalsIgnoreCase(mSMP) && ret.getIsSMP() == 1)
                || (generaltxt.equalsIgnoreCase(mCompertior) && ret.getOwn() == 0)
                || (generaltxt.equalsIgnoreCase(mShelf) && ((ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > 0) || ret.getLocations().get(mSelectedLocationIndex).getAvailability() > -1));
    }

    /**
     * @param product productMasterBO object
     * @return total quantity
     * @author rajesh.k update total value in lisview
     */
    public int getProductTotalValue(ProductMasterBO product) {
        int totalQty = 0;
        Vector<StandardListBO> locationList = businessModel.productHelper
                .getInStoreLocation();

        int size = locationList.size();
        for (int i = 0; i < size; i++) {

            if (product.getLocations().get(i).getShelfPiece() > -1)
                totalQty += product.getLocations().get(i).getShelfPiece();
            if (product.getLocations().get(i).getShelfCase() > -1)
                totalQty += (product.getLocations().get(i).getShelfCase() * product
                        .getCaseSize());
            if (product.getLocations().get(i).getShelfOuter() > -1)
                totalQty += (product.getLocations().get(i).getShelfOuter() * product
                        .getOutersize());
/*
            if (product.getLocations().get(i).getAvailability() > -1)
                totalQty += product.getLocations().get(i).getAvailability();*/
        }
        return totalQty;

    }


    public void updateGeneralText(String mFilterText) {
        fiveFilter_productIDs = null;
        if (mFilterText.equals("")) {
            mFilterText = GENERAL;
        }
        generalButton = mFilterText;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();
    }

    public ArrayAdapter<ReasonMaster> getSpinnerAdapter() {
        return spinnerAdapter;
    }

    /**
     * Populate list with specific reason type of the module.
     */
    public void prepareAdapters() {

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(context.getResources().getString(R.string.product_name));
        mSearchTypeArray.add(context.getResources().getString(R.string.order_gcas));
        mSearchTypeArray.add(context.getResources().getString(
                R.string.order_dialog_barcode));

        //location
        mLocationAdapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_singlechoice);
        for (StandardListBO temp : businessModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (businessModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = businessModel.productHelper.getmSelectedGLobalLocationIndex();
        }

        //reasons
        spinnerAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : businessModel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("INVT")
                    || temp.getReasonCategory().equalsIgnoreCase("NONE"))
                spinnerAdapter.add(temp);
        }
    }

    public ArrayAdapter<StandardListBO> getLocationAdapter() {
        return mLocationAdapter;
    }

    private Vector<ProductMasterBO> getTaggedProducts() {
        return businessModel.productHelper.getTaggedProducts();
    }

    public void updateBrandText() {
        try {

            String generalTxt = generalButton;

            ArrayList<ProductMasterBO> stockList = new ArrayList<>();
            Vector<ProductMasterBO> items = getTaggedProducts();
            if (items == null) {
                stockCheckView.showAlert();
                return;
            }


            if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {

                for (ProductMasterBO sku : items) {
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (sku.getIsSaleable() == 1 && sku.getOwn() == 1) {
                            if (isSpecialFilter_enabled) {
                                if (isSpecialFilterAppliedProduct(generalTxt, sku))
                                    stockList.add(sku);
                            } else {
                                stockList.add(sku);
                            }
                        }
                    }
                }
            } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
                for (ProductMasterBO sku : items) {
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (sku.getIsSaleable() == 1 && sku.getOwn() == 0) {
                            if (isSpecialFilter_enabled) {
                                if (isSpecialFilterAppliedProduct(generalTxt, sku))
                                    stockList.add(sku);
                            } else {
                                stockList.add(sku);
                            }
                        }
                    }
                }
            } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
                for (ProductMasterBO sku : items) {
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (sku.getIsSaleable() == 1) {
                            if (isSpecialFilter_enabled) {
                                if (isSpecialFilterAppliedProduct(generalTxt, sku))
                                    stockList.add(sku);
                            } else {
                                stockList.add(sku);
                            }
                        }
                    }
                }
            }
            mCompetitorSelectedIdByLevelId = new HashMap<>();
            stockCheckView.updateListFromFilter(stockList);
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public void loadSearchedList(String s) {
        ProductMasterBO ret;

        if (s.length() >= 3) {

            Vector<ProductMasterBO> items = new Vector<>();
            if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
                for (ProductMasterBO productBo : getTaggedProducts()) {
                    if (productBo.getIsSaleable() == 1 && productBo.getOwn() == 1)
                        items.add(productBo);
                }
            } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
                for (ProductMasterBO productBo : getTaggedProducts()) {
                    if (productBo.getIsSaleable() == 1 && productBo.getOwn() == 0)
                        items.add(productBo);
                }
            } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
                items = getTaggedProducts();
            }

            if (items.isEmpty()) {
                stockCheckView.showAlert();
                return;
            }
            int siz = items.size();

            ArrayList<ProductMasterBO> stockList = new ArrayList<>();
            String mSelectedFilter = businessModel.getProductFilter();

            for (int i = 0; i < siz; ++i) {
                ret = items.elementAt(i);

                if (mSelectedFilter.equals(context.getResources().getString(
                        R.string.order_dialog_barcode))) {

                    if (ret.getBarCode() != null
                            && (ret.getBarCode().toLowerCase()
                            .contains(s.toLowerCase())
                            || ret.getCasebarcode().toLowerCase().
                            contains(s.toLowerCase())
                            || ret.getOuterbarcode().toLowerCase().
                            contains(s.toLowerCase())) && ret.getIsSaleable() == 1) {

                        if (generalButton.equalsIgnoreCase(GENERAL))//No filters selected
                            stockList.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            stockList.add(ret);
                    }
                } else if (mSelectedFilter.equals(context.getResources().getString(
                        R.string.order_gcas))) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    s.toLowerCase()) && ret.getIsSaleable() == 1) {
                        if (generalButton.equalsIgnoreCase(GENERAL))//No filters selected
                            stockList.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            stockList.add(ret);
                    }
                } else if (mSelectedFilter.equals(context.getResources().getString(
                        R.string.product_name))) {
                    if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    s.toLowerCase()) && ret.getIsSaleable() == 1)
                        if (generalButton.equalsIgnoreCase(GENERAL))//No filters selected
                            stockList.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            stockList.add(ret);
                }

            }

            stockCheckView.updateListFromFilter(stockList);

        } else if (s.length() == 0) {
            loadProductList();
        } else {
            stockCheckView.showSearchValidationToast();
        }
    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
        if (!GENERAL.equals(generalButton)) {
            // both filter selected
            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
                        && isSpecialFilterAppliedProduct(generalButton, ret))
                    return true;
            } else {
                if (isSpecialFilterAppliedProduct(generalButton, ret))
                    return true;
            }
        } else if (GENERAL.equals(generalButton)) {
            // product filter alone selected
            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID()))
                    return true;
            } else {
                if (isSpecialFilterAppliedProduct(generalButton, ret))
                    return true;
            }
        }
        return false;
    }

    public void loadProductList() {
        try {
            Vector<ProductMasterBO> items = getTaggedProducts();

            int siz = items.size();
            ArrayList<ProductMasterBO> stockList = new ArrayList<>();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getIsSaleable() == 1) {
                    if (generalButton.equalsIgnoreCase(GENERAL))//No filters selected
                        stockList.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        stockList.add(ret);
                }
            }
            stockCheckView.updateListFromFilter(stockList);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * used to hide the specialFilter
     */
    public void hideSpecialFilter() {
        isSpecialFilter_enabled = false;
        generalButton = "GENERAL";

    }

    public void hideRemarksButton() {
        remarks_button_enable = false;
    }

    /*
    * Update competitor filtered products
    * */

    public void updateCompetitorFilteredProducts(Vector<CompetitorFilterLevelBO> parentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String filterText) {
        //  this.mCompetitorSelectedIdByLevelId=mSelectedIdByLevelId;
        this.mSelectedIdByLevelId = new HashMap<>();// clearing product filter
        //  this.filtertext = filterText;

        ArrayList<ProductMasterBO> stockList = new ArrayList<>();
        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();
        if (parentIdList != null && !parentIdList.isEmpty()) {
            for (CompetitorFilterLevelBO mParentBO : parentIdList) {
                for (ProductMasterBO sku : items) {
                    if (mParentBO.getProductId() == sku.getCompParentId()) {
                        stockList.add(sku);
                    }
                }
            }
        } else {
            stockList.addAll(items);
        }

        stockCheckView.updateListFromFilter(stockList);
        stockCheckView.scrollToSelectedTabPosition();
        generalButton = GENERAL;
        putValueToFilterMap("");

    }


    /**
     * Load selected reason name in the Screen
     *
     * @param reasonId reason id for which the index need to be found
     * @return position of the reason id
     */
    public int getReasonIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    public void returnToHome() {
        businessModel.productHelper.clearOrderTable();
        businessModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));
    }
}
