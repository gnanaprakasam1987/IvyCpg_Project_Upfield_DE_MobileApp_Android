package com.ivy.cpg.view.order;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;

import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.ProductSearchCallBack;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

public class ProductSearch {

    private Context context;
    private EditText mEdt_searchproductName;
    private Vector<ProductMasterBO> masterList,searchedList;
    private BusinessModel bModel;
    private int loadStockedProduct=-1;
    private SearchAsync searchAsync;
    private String selectedSpecialFilter;
    private String userEnteredText;
    private static ProductSearch instance=null;
    private ProductSearchCallBack productSearchCallBack;
    public static String SCREEN_CODE_ORDER="ORDER";
    private String current_screen_code;
    private static int SEARCH_BY_GIVEN_TEXT=1;
    private static int SEARCH_BY_GIVEN_PRODUCT_ID=2;
    private static int SEARCH_BY_GIVEN_PRODUCT_ID_AND_ATTRIBUTE=3;
    private static int SEARCH_BY_SPECIAL_FILTER=4;
    private static int SHOW_ALL_PRODUCTS=5;

    private static final String ALL = "ALL";

    private ProductHelper productHelper;

    private int productId;
    private ArrayList<Integer> attributeProductIds;

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
    final String mStock = "Filt17";
    final String mDiscount = "Filt18";
    final String mSuggestedOrder = "Filt25";
    final String mDrugProducts = "Filt28";
    final String mDeadProducts = "Filt15";

    final String mCommon = "Filt01";
    final String mCompertior = "Filt23";
    final String mFocusBrand3 = "Filt20";
    final String mFocusBrand4 = "Filt21";
    final String mSMP = "Filt22";
    final String mNearExpiryTag = "Filt19";
    final String mShelf = "Filt24";

    final String GENERAL = "General";

    private boolean isSpecialFilter,isProductFilter,isUserEntryFilter;
    public ProductSearch(Context context,BusinessModel bModel,String current_screen_code){
        this.context=context;
        this.bModel=bModel;
        this.current_screen_code=current_screen_code;
        productHelper=ProductHelper.getInstance(context);
        if (productSearchCallBack == null) {
            if (context instanceof ProductSearchCallBack) {
                this.productSearchCallBack = (ProductSearchCallBack) context;
            }
        }

    }

      public static ProductSearch getInstance(Context context,BusinessModel bModel,String current_screen_code) {

        if (instance == null) {
            instance = new ProductSearch(context,bModel,current_screen_code);
        }
        return instance;
    }


    public void startSearch(Vector<ProductMasterBO> masterList,String stringToFilter)
    {

        isUserEntryFilter=true;

        this.masterList=masterList;
        this.userEnteredText =stringToFilter;

        searchAsync = new SearchAsync();
        searchAsync.execute();

    }


    public void startSearch(Vector<ProductMasterBO> masterList,int productId, ArrayList<Integer> attributeProductIds)
    {
        if (bModel.configurationMasterHelper.IS_UNLINK_FILTERS) {
            isSpecialFilter = false;
        }
        isProductFilter=true;

        this.masterList=masterList;
        this.attributeProductIds=attributeProductIds;
        this.productId=productId;

            searchAsync = new SearchAsync();
            searchAsync.execute();

    }

    public void startSpecialFilterSearch(Vector<ProductMasterBO> masterList,String selectedSpecialFilter){

        isSpecialFilter=true;
         isProductFilter=false;// clearing other filter

        if(selectedSpecialFilter.equalsIgnoreCase(GENERAL))
            isSpecialFilter=false;

        this.masterList=masterList;
        this.selectedSpecialFilter =selectedSpecialFilter;

        searchAsync = new SearchAsync();
        searchAsync.execute();

    }

    public void getAllProducts(Vector<ProductMasterBO> masterList){

        isUserEntryFilter=false;

        this.masterList=masterList;

        searchAsync = new SearchAsync();
        searchAsync.execute();
    }


    public void cancelSearch(){
        if (searchAsync!=null&&searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
            searchAsync.cancel(true);
        }
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
            productSearchCallBack.productSearchResult(searchedList);

        }
    }

    private void loadSearchedList() {
        try {

            searchedList=new Vector<>();

            for (int i = 0; i < masterList.size(); ++i) {
                ProductMasterBO productMasterBO = masterList.elementAt(i);

                if (searchAsync.isCancelled()) {
                    break;
                }

                if(productMasterBO.getIsSaleable() == 1&&isValidProductForCurrentScreen(productMasterBO)) {

                    if(!isSpecialFilter|| (isSpecialFilter&&isSpecialFilterAppliedProduct(selectedSpecialFilter,productMasterBO))){

                        if(!isProductFilter ||(isProductFilter&&isParentHierarchyMatches(productMasterBO))){

                            if(!isUserEntryFilter||(isUserEntryFilter&&isUserEntryFilterMatches(productMasterBO))){

                                searchedList.add(productMasterBO);

                            }


                        }

                    }


                }


            }


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean isValidProductForCurrentScreen(ProductMasterBO productMasterBO){


        if(SCREEN_CODE_ORDER.equals(current_screen_code)) {

            if (bModel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productMasterBO.getGroupid() == 0)
                return false;

            OrderHelper orderHelper=OrderHelper.getInstance(context);
            if (bModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !orderHelper.isQuickCall &&
                    !productMasterBO.getParentHierarchy().contains("/" + bModel.productHelper.getmSelectedGlobalProductId() + "/"))
                return false;

            if (bModel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY){
                int flag;
                if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                    flag = bModel.getRetailerMasterBO().getIsVansales() == 1 ? 1 : 0;
                } else {
                    flag = bModel.configurationMasterHelper.IS_INVOICE ? 1 : 0;
                }

                if((flag==1&&productMasterBO.getSIH()<=0)||(flag==0&&productMasterBO.getWSIH()<=0))
                    return false;

            }

            if (bModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER && productMasterBO.getIndicativeOrder_oc() <= 0)
                return false;

        }


        return true;

    }

    private void increaseOrderQty(ProductMasterBO productMasterBO){

                if (mEdt_searchproductName.getText().toString().equals(productMasterBO.getBarCode())) {
                    productMasterBO.setOrderedPcsQty(productMasterBO.getOrderedPcsQty() + 1);
                } else if (mEdt_searchproductName.getText().toString().equals(productMasterBO.getCasebarcode())) {
                    productMasterBO.setOrderedCaseQty(productMasterBO.getOrderedCaseQty() + 1);
                } else if (mEdt_searchproductName.getText().toString().equals(productMasterBO.getOuterbarcode())) {
                    productMasterBO.setOrderedOuterQty(productMasterBO.getOrderedOuterQty() + 1);
                }

    }

    private boolean isSpecialFilterAppliedProduct(String selectedFilter, ProductMasterBO ret) {

        final String GENERAL = "General";

        return selectedFilter.equalsIgnoreCase(GENERAL)
                || (selectedFilter.equalsIgnoreCase(mSbd) && ret.isRPS())
                || (selectedFilter.equalsIgnoreCase(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (selectedFilter.equalsIgnoreCase(mPurchased) && ret.getIsPurchased() == 1)
                || (selectedFilter.equalsIgnoreCase(mInitiative) && ret.getIsInitiativeProduct() == 1)
                || (selectedFilter.equalsIgnoreCase(mCommon) && applyCommonFilterConfig(ret))
                || (selectedFilter.equalsIgnoreCase(mSbdGaps) && (ret.isRPS() && !ret.isSBDAcheived()))
                || (selectedFilter.equalsIgnoreCase(mInStock) && ret.getWSIH() > 0)
                || (selectedFilter.equalsIgnoreCase(mOnAllocation) && ret.isAllocation() == 1 && bModel.configurationMasterHelper.IS_SIH_VALIDATION)
                || (selectedFilter.equalsIgnoreCase(mPromo) && ret.isPromo())
                || (selectedFilter.equalsIgnoreCase(mMustSell) && ret.getIsMustSell() == 1)
                || (selectedFilter.equalsIgnoreCase(mFocusBrand) && ret.getIsFocusBrand() == 1)
                || (selectedFilter.equalsIgnoreCase(mFocusBrand2) && ret.getIsFocusBrand2() == 1)
                || (selectedFilter.equalsIgnoreCase(msih) && ret.getSIH() > 0)
                || (selectedFilter.equalsIgnoreCase(mOOS) && ret.getOos() == 0)
                || (selectedFilter.equalsIgnoreCase(mNMustSell) && ret.getIsNMustSell() == 1)
                || (selectedFilter.equalsIgnoreCase(mDiscount) && ret.getIsDiscountable() == 1)
                || (selectedFilter.equalsIgnoreCase(mStock) && (ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfPiece() > -1 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfCase() > -1 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfOuter() > -1 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getWHPiece() > 0 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getWHCase() > 0 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getWHOuter() > 0 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getAvailability() > -1))
                || (selectedFilter.equalsIgnoreCase(mNearExpiryTag) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (selectedFilter.equalsIgnoreCase(mFocusBrand3) && ret.getIsFocusBrand3() == 1)
                || (selectedFilter.equalsIgnoreCase(mFocusBrand4) && ret.getIsFocusBrand4() == 1)
                || (selectedFilter.equalsIgnoreCase(mSMP) && ret.getIsSMP() == 1)
                || (selectedFilter.equalsIgnoreCase(mCompertior) && ret.getOwn() == 0)
                || (selectedFilter.equalsIgnoreCase(mShelf) && (ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfCase() > -1 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfPiece() > -1 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfOuter() > -1 || ret.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getAvailability() > -1))
                || (selectedFilter.equalsIgnoreCase(mSuggestedOrder) && ret.getSoInventory() > 0)
                || (selectedFilter.equalsIgnoreCase(mDrugProducts) && ret.getIsDrug() == 1)
                || (selectedFilter.equalsIgnoreCase(mDeadProducts) && ret.getmDeadProduct() == 1);
    }

    private boolean applyCommonFilterConfig(ProductMasterBO ret) {

        for (ConfigureBO bo : bModel.configurationMasterHelper.getGenFilter()) {
            if (bo.getMandatory() == 1) {

                return (bo.getConfigCode().equals(mSbd) && ret.isRPS()) || (bo.getConfigCode().equals(mSbdGaps) && ret.isRPS() && !ret.isSBDAcheived()) || (bo.getConfigCode().equals(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                        || (bo.getConfigCode().equals(mPurchased) && ret.getIsPurchased() == 1) || (bo.getConfigCode().equals(mInitiative) && ret.getIsInitiativeProduct() == 1) || (bo.getConfigCode().equals(mOnAllocation) && ret.isAllocation() == 1 && bModel.configurationMasterHelper.IS_SIH_VALIDATION)
                        || (bo.getConfigCode().equals(mInStock) && ret.getWSIH() > 0) || (bo.getConfigCode().equals(mPromo) && ret.isPromo()) || (bo.getConfigCode().equals(mMustSell) && ret.getIsMustSell() == 1)
                        || (bo.getConfigCode().equals(mFocusBrand)) || (bo.getConfigCode().equals(mFocusBrand2) && ret.getIsFocusBrand2() == 1) || (bo.getConfigCode().equals(msih) && ret.getSIH() > 0) || (bo.getConfigCode().equals(mOOS) && ret.getOos() == 0)
                        || (bo.getConfigCode().equals(mNMustSell) && ret.getIsNMustSell() == 1) || (bo.getConfigCode().equals(mStock) && (ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfPiece() > -1
                        || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfCase() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfOuter() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getWHPiece() > 0
                        || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getWHCase() > 0 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getWHOuter() > 0 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getAvailability() > -1))
                        || (bo.getConfigCode().equals(mDiscount) && ret.getIsDiscountable() == 1) || (bo.getConfigCode().equals(mDrugProducts) && ret.getIsDrug() == 1)
                        || (bo.getConfigCode().equals(mDeadProducts) && ret.getmDeadProduct() == 1);

            }
        }
        return false;


    }

    private boolean isParentHierarchyMatches(ProductMasterBO productMasterBO){
        if ((productId!=0||productMasterBO.getParentHierarchy().contains("/" + productId + "/"))
                &&attributeProductIds!=null&&attributeProductIds.contains(SDUtil.convertToInt(productMasterBO.getProductID()))) {

            return true;

        }
        else if (productMasterBO.getParentHierarchy().contains("/" + productId + "/")) {

            return true;

        }

        return false;
    }

    private boolean isUserEntryFilterMatches(ProductMasterBO productMasterBO){
        if (productMasterBO.getProductShortName() != null && productMasterBO.getProductShortName()
                .toLowerCase()
                .contains(userEnteredText)) {
            return true;
        } else if (productMasterBO.getBarCode() != null && (productMasterBO.getBarCode().toLowerCase().contains(userEnteredText.toLowerCase())
                || productMasterBO.getCasebarcode().toLowerCase().contains(userEnteredText.toLowerCase())
                || productMasterBO.getOuterbarcode().toLowerCase().contains(userEnteredText.toLowerCase()))) {

            if (SCREEN_CODE_ORDER.equals(current_screen_code) && bModel.configurationMasterHelper.IS_QTY_INCREASE) {
                increaseOrderQty(productMasterBO);
            }

            return true;

        } else if (((productMasterBO.getRField1() != null && productMasterBO.getRField1().toLowerCase().contains(userEnteredText.toLowerCase()))
                || (productMasterBO.getProductCode() != null && productMasterBO.getProductCode().toLowerCase().contains(userEnteredText.toLowerCase())))) {
            return true;
        }

        return false;
    }

    public boolean isSpecialFilter() {
        return isSpecialFilter;
    }

    public boolean isProductFilter() {
        return isProductFilter;
    }


    public boolean isUserEntryFilter() {
        return isUserEntryFilter;
    }


}
