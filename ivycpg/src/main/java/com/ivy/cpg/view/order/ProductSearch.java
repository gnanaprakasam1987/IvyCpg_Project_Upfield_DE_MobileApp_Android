package com.ivy.cpg.view.order;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.ProductSearchCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Vector;

public class ProductSearch implements View.OnClickListener,TextView.OnEditorActionListener,SpeechResultListener {

    private Context context;
    private EditText editText_productName;
    private Vector<ProductMasterBO> masterList,searchedList;
    private BusinessModel bModel;
    private SearchAsync searchAsync;
    private String selectedSpecialFilter;
    private String userEnteredText;
    private ProductSearchCallBack productSearchCallBack;
    public static String SCREEN_CODE_ORDER="ORDER";
    private String current_screen_code;

    private ProductHelper productHelper;

    private int productId;
    private ArrayList<Integer> attributeProductIds;

    private static final String FILTER_CODE_SBD = "Filt02";
    private static final String FILTER_CODE_SBD_GAPS = "Filt03";
    public static final String FILTER_CODE_ORDERED = "Filt04";
    private static final String FILTER_CODE_PURCHASED = "Filt05";
    private static final String FILTER_CODE_INITIATIVE = "Filt06";
    private static final String FILTER_CODE_ALLOCATION = "Filt07";
    private static final String FILTER_CODE_WAREHOUSE_STOCK = "Filt08";
    private static final String FILTER_CODE_PROMO = "Filt09";
    private static final String FILTER_CODE_MUSTSELL = "Filt10";
    private static final String FILTER_CODE_FOCUSBRAND = "Filt11";
    private static final String FILTER_CODE_FOCUSBRAND2 = "Filt12";
    private static final String FILTER_CODE_VAN_STOCK = "Filt13";
    private static final String FILTER_CODE_OUT_OF_STOCK = "Filt14";
    private static final String FILTER_CODE_NON_MUSTSELL = "Filt16";
    private static final String FILTER_CODE_SHELF_STOCK = "Filt17";
    private static final String FILTER_CODE_DISCOUNTABLE_PRODUCTS = "Filt18";
    public static final String FILTER_CODE_SUGGESTED_ORDER = "Filt25";
    private static final String FILTER_CODE_DEAD_PRODUCTS = "Filt15";
    private static final String FILTER_CODE_COMMON = "Filt01";
    private static final String FILTER_CODE_COMPETITOR_PRODUCTS = "Filt23";
    private static final String FILTER_CODE_FOCUSBRAND3 = "Filt20";
    private static final String FILTER_CODE_DRUG_PRODUCTS = "Filt28";
    private static final String FILTER_CODE_FOCUSBRAND4 = "Filt21";
    private static final String FILTER_CODE_SMALL_PACK_PRODUCTS = "Filt22";
    private static final String FILTER_CODE_NEAR_EXPIRY = "Filt19";
    private static final String FILTER_CODE_SHELF_STOCK_1 = "Filt24";
    private static final String FILTER_CODE_TRADE_PROMOTION = "Filt31";

    private final String GENERAL = "General";

    private SpeechToVoiceDialog speechToVoiceDialog;
    private ViewFlipper viewFlipper;
    private Button mBtn_Search;
    private Button mBtn_clear;

    private TextView textView_productName;

    private boolean isSpecialFilter,isProductFilter,isUserEntryFilter;
    ConfigurationMasterHelper configurationMasterHelper;
    public ProductSearch(Context context,Vector<ProductMasterBO> list,String current_screen_code){
        this.context=context;
        this.masterList=list;
        this.bModel=(BusinessModel)context.getApplicationContext();
        configurationMasterHelper=ConfigurationMasterHelper.getInstance(context);
        this.current_screen_code=current_screen_code;
        productHelper=ProductHelper.getInstance(context);
        if (productSearchCallBack == null) {
            if (context instanceof ProductSearchCallBack) {
                this.productSearchCallBack = (ProductSearchCallBack) context;
            }
        }

        textView_productName = ((Activity)context).findViewById(R.id.productName);
        textView_productName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT)); // apply in xml
        editText_productName =  ((Activity)context).findViewById(R.id.edt_searchproductName);
        mBtn_Search =  ((Activity)context).findViewById(R.id.btn_search);
        viewFlipper =  ((Activity)context).findViewById(R.id.view_flipper);
        mBtn_clear =  ((Activity)context).findViewById(R.id.btn_clear);

        if (configurationMasterHelper.IS_VOICE_TO_TEXT == -1)
            ((Activity)context).findViewById(R.id.btn_speech).setVisibility(View.GONE);

        mBtn_Search.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        editText_productName.setOnEditorActionListener(this);
        editText_productName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));


        editText_productName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {

                    startSearch(masterList,s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                cancelSearch();
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });

        ((Activity)context).findViewById(R.id.btn_speech).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int permissionStatus = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {


                    speechToVoiceDialog = new SpeechToVoiceDialog(ProductSearch.this);
                    speechToVoiceDialog.setCancelable(true);
                    speechToVoiceDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "SPEECH_TO_TEXT");

                    if (viewFlipper.getDisplayedChild() == 0) {
                        viewFlipper.showNext();
                    };


                } else {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.permission_enable_msg)
                                    + " " + context.getResources().getString(R.string.record_audio)
                            , Toast.LENGTH_LONG).show();
                }


            }
        });

    }


    public void startSearch(Vector<ProductMasterBO> masterList,String stringToFilter)
    {

        if(!stringToFilter.equals("")) {
            isUserEntryFilter = true;

            this.masterList = masterList;
            this.userEnteredText = stringToFilter;
        }
        else isUserEntryFilter=false;

        searchAsync = new SearchAsync();
        searchAsync.execute();

    }


    public void startSearch(Vector<ProductMasterBO> masterList,int productId, ArrayList<Integer> attributeProductIds)
    {
        if (configurationMasterHelper.IS_UNLINK_FILTERS) {
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



    private void cancelSearch(){
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

            if (configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
                getProductInUnipalSequence();

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

                if(isValidProductForCurrentScreen(productMasterBO)) {

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


        if(!configurationMasterHelper.SHOW_NON_SALABLE_PRODUCT&&productMasterBO.getIsSaleable() == 0)
            return false;

            if (SCREEN_CODE_ORDER.equals(current_screen_code)) {

                if (configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productMasterBO.getGroupid() == 0)
                    return false;

                OrderHelper orderHelper = OrderHelper.getInstance(context);
                if (configurationMasterHelper.IS_GLOBAL_CATEGORY && !orderHelper.isQuickCall &&
                        !productMasterBO.getParentHierarchy().contains("/" + productHelper.getmSelectedGlobalProductId() + "/"))
                    return false;

                if (configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY) {
                    int flag;
                    if (configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        flag = bModel.getAppDataProvider().getRetailMaster().getIsVansales() == 1 ? 1 : 0;
                    } else {
                        flag = configurationMasterHelper.IS_INVOICE ? 1 : 0;
                    }

                    if ((flag == 1 && productMasterBO.getSIH() <= 0) || (flag == 0 && productMasterBO.getWSIH() <= 0))
                        return false;

                }

                if (configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER && (productMasterBO.getIndicativeOrder_op() + productMasterBO.getIndicativeOrder_oc() + productMasterBO.getIndicativeOrder_oo()) <= 0)
                    return false;

            }



        return true;

    }

    private void increaseOrderQty(ProductMasterBO productMasterBO){

                if (editText_productName.getText().toString().equals(productMasterBO.getBarCode())) {
                    productMasterBO.setOrderedPcsQty(productMasterBO.getOrderedPcsQty() + 1);
                } else if (editText_productName.getText().toString().equals(productMasterBO.getCasebarcode())) {
                    productMasterBO.setOrderedCaseQty(productMasterBO.getOrderedCaseQty() + 1);
                } else if (editText_productName.getText().toString().equals(productMasterBO.getOuterbarcode())) {
                    productMasterBO.setOrderedOuterQty(productMasterBO.getOrderedOuterQty() + 1);
                }

    }

    public boolean isSpecialFilterAppliedProduct(String selectedFilter, ProductMasterBO ret) {

        final String GENERAL = "General";

        return selectedFilter.equalsIgnoreCase(GENERAL)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_SBD) && ret.isRPS())
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_ORDERED) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_PURCHASED) && ret.getIsPurchased() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_INITIATIVE) && ret.getIsInitiativeProduct() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_COMMON) && applyCommonFilterConfig(ret))
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_SBD_GAPS) && (ret.isRPS() && !ret.isSBDAcheived()))
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_WAREHOUSE_STOCK) && ret.getWSIH() > 0)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_ALLOCATION) && ret.isAllocation() == 1 && configurationMasterHelper.IS_SIH_VALIDATION)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_PROMO) && ret.isPromo())
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_MUSTSELL) && ret.getIsMustSell() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_FOCUSBRAND) && ret.getIsFocusBrand() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_FOCUSBRAND2) && ret.getIsFocusBrand2() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_VAN_STOCK) && ret.getSIH() > 0)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_OUT_OF_STOCK) && ret.getOos() == 0)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_NON_MUSTSELL) && ret.getIsNMustSell() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_DISCOUNTABLE_PRODUCTS) && ret.getIsDiscountable() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_NEAR_EXPIRY) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_FOCUSBRAND3) && ret.getIsFocusBrand3() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_FOCUSBRAND4) && ret.getIsFocusBrand4() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_SMALL_PACK_PRODUCTS) && ret.getIsSMP() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_COMPETITOR_PRODUCTS) && ret.getOwn() == 0)
                || ((selectedFilter.equalsIgnoreCase(FILTER_CODE_SHELF_STOCK)||selectedFilter.equalsIgnoreCase(FILTER_CODE_SHELF_STOCK_1)) && (ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfCase() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfPiece() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfOuter() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getAvailability() > -1))
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_SUGGESTED_ORDER) && ret.getSoInventory() > 0)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_DRUG_PRODUCTS) && ret.getIsDrug() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_DEAD_PRODUCTS) && ret.getmDeadProduct() == 1)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_TRADE_PROMOTION) && ret.getmTradePromotion() == 1);
    }

    private boolean applyCommonFilterConfig(ProductMasterBO ret) {

        for (ConfigureBO bo : configurationMasterHelper.getGenFilter()) {
            if (bo.getMandatory() == 1) {

                return (bo.getConfigCode().equals(FILTER_CODE_SBD) && ret.isRPS()) || (bo.getConfigCode().equals(FILTER_CODE_SBD_GAPS) && ret.isRPS() && !ret.isSBDAcheived()) || (bo.getConfigCode().equals(FILTER_CODE_ORDERED) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                        || (bo.getConfigCode().equals(FILTER_CODE_PURCHASED) && ret.getIsPurchased() == 1) || (bo.getConfigCode().equals(FILTER_CODE_INITIATIVE) && ret.getIsInitiativeProduct() == 1) || (bo.getConfigCode().equals(FILTER_CODE_ALLOCATION) && ret.isAllocation() == 1 && configurationMasterHelper.IS_SIH_VALIDATION)
                        || (bo.getConfigCode().equals(FILTER_CODE_WAREHOUSE_STOCK) && ret.getWSIH() > 0) || (bo.getConfigCode().equals(FILTER_CODE_PROMO) && ret.isPromo()) || (bo.getConfigCode().equals(FILTER_CODE_MUSTSELL) && ret.getIsMustSell() == 1)
                        || (bo.getConfigCode().equals(FILTER_CODE_FOCUSBRAND)) || (bo.getConfigCode().equals(FILTER_CODE_FOCUSBRAND2) && ret.getIsFocusBrand2() == 1) || (bo.getConfigCode().equals(FILTER_CODE_VAN_STOCK) && ret.getSIH() > 0) || (bo.getConfigCode().equals(FILTER_CODE_OUT_OF_STOCK) && ret.getOos() == 0)
                        || (bo.getConfigCode().equals(FILTER_CODE_NON_MUSTSELL) && ret.getIsNMustSell() == 1) || (bo.getConfigCode().equals(FILTER_CODE_SHELF_STOCK) && (ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfPiece() > -1
                        || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfCase() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getShelfOuter() > -1 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getWHPiece() > 0
                        || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getWHCase() > 0 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getWHOuter() > 0 || ret.getLocations().get(productHelper.getmSelectedLocationIndex()).getAvailability() > -1))
                        || (bo.getConfigCode().equals(FILTER_CODE_DISCOUNTABLE_PRODUCTS) && ret.getIsDiscountable() == 1) || (bo.getConfigCode().equals(FILTER_CODE_DRUG_PRODUCTS) && ret.getIsDrug() == 1)
                        || (bo.getConfigCode().equals(FILTER_CODE_DEAD_PRODUCTS) && ret.getmDeadProduct() == 1) || (bo.getConfigCode().equals(FILTER_CODE_TRADE_PROMOTION) && ret.getmTradePromotion() == 1);

            }
        }
        return false;


    }

    private boolean isParentHierarchyMatches(ProductMasterBO productMasterBO){
        if (productMasterBO.getParentHierarchy().contains("/" + productId + "/")
                &&attributeProductIds!=null&&attributeProductIds.contains(SDUtil.convertToInt(productMasterBO.getProductID()))) {

            return true;

        }
        else if (productMasterBO.getParentHierarchy().contains("/" + productId + "/")) {

            return true;

        }
        else if (attributeProductIds!=null&&attributeProductIds.contains(SDUtil.convertToInt(productMasterBO.getProductID()))) {

            return true;

        }
        else if (productId==0) {

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

            if (SCREEN_CODE_ORDER.equals(current_screen_code) && configurationMasterHelper.IS_QTY_INCREASE) {
                increaseOrderQty(productMasterBO);
            }

            return true;

        } else if (((productMasterBO.getRField1() != null && productMasterBO.getRField1().toLowerCase().contains(userEnteredText.toLowerCase()))
                || (productMasterBO.getProductCode() != null && productMasterBO.getProductCode().toLowerCase().contains(userEnteredText.toLowerCase())))) {
            return true;
        }

        return false;
    }

    //Product sequence for Unipal
    private void getProductInUnipalSequence() {
        if(searchedList.size()>0) {
            ArrayList<String> seqIDList = new ArrayList<>();
            LinkedHashSet<String> hs = new LinkedHashSet<>();
            Vector<ProductMasterBO> items = searchedList;
            Vector<ProductMasterBO> newProductList = new Vector<>();
            for (ProductMasterBO productMasterBO : items) {
                for (int j = 0; j < productMasterBO.getLocations().size(); j++) {
                    if (productMasterBO.isRPS() && (productMasterBO.getLocations().get(j).getShelfPiece() == -1
                            || productMasterBO.getLocations().get(j).getShelfCase() == -1
                            || productMasterBO.getLocations().get(j).getShelfOuter() == -1
                            || productMasterBO.getLocations().get(j).getAvailability() == -1)) {
                        hs.add(productMasterBO.getProductID());
                    }
                }
                if (productMasterBO.isRPS() && productMasterBO.isSBDAcheived())
                    hs.add(productMasterBO.getProductID());
                if (productMasterBO.getIsInitiativeProduct() == 1)
                    hs.add(productMasterBO.getProductID());
                if (productMasterBO.isPromo())
                    hs.add(productMasterBO.getProductID());
            }
            seqIDList.addAll(hs);

            for (int i = 0; i < seqIDList.size(); i++) {
                String tempId = seqIDList.get(i);
                for (int j = 0; j < items.size(); j++) {
                    ProductMasterBO productMasterBO = items.get(j);
                    String prodID = productMasterBO.getProductID();
                    if (prodID.equals(tempId)) {
                        newProductList.add(i, productMasterBO);
                    }
                }
            }

            for (ProductMasterBO productMasterBO : items) {
                if (!newProductList.contains(productMasterBO)) {
                    newProductList.add(productMasterBO);
                }
            }
            searchedList.clear();
            searchedList.addAll(newProductList);
        }
    }

    public String getDefaultSpecialFilter() {
        String defaultFilter = "";
        try {
            Vector<ConfigureBO> specialFilterList = configurationMasterHelper
                    .getGenFilter();
            for (int i = 0; i < specialFilterList.size(); i++) {
                if (specialFilterList.get(i).getHasLink() == 1) {
                    if (!configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        defaultFilter = specialFilterList.get(i).getConfigCode();
                        break;
                    } else {
                        if (bModel.getAppDataProvider().getRetailMaster().getIsVansales() == 1) {
                            if (specialFilterList.get(i).getConfigCode().equals(FILTER_CODE_VAN_STOCK)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
                                break;
                            } else if (!specialFilterList.get(i).getConfigCode().equals(FILTER_CODE_WAREHOUSE_STOCK)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
                                break;
                            }
                        } else {
                            if (specialFilterList.get(i).getConfigCode().equals(FILTER_CODE_WAREHOUSE_STOCK)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
                                break;
                            } else if (!specialFilterList.get(i).getConfigCode().equals(FILTER_CODE_VAN_STOCK)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
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

    public boolean isSpecialFilter() {
        return isSpecialFilter;
    }

    public boolean isProductFilter() {
        return isProductFilter;
    }


    public boolean isUserEntryFilter() {
        return isUserEntryFilter;
    }


    public void onClick(View v) {
        Button vw = (Button) v;

        if (vw == mBtn_Search) {
            viewFlipper.showNext();
            editText_productName.requestFocus();
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(editText_productName, InputMethodManager.SHOW_FORCED);
            } catch (Exception e) {
                Commons.printException(e);
            }

        } else if (vw == mBtn_clear) {
            viewFlipper.showPrevious();
            editText_productName.setText("");
            //productName.setText("");
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((Activity)context).getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }

            startSearch(masterList,"");

        }
    }

    public void hideSoftInputFromWindow(){
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                editText_productName.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (editText_productName.getText().length() >= 3) {

                startSearch(masterList, editText_productName.getText().toString());
            } else {
                Toast.makeText(context, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
            InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(((Activity)context).getCurrentFocus().getWindowToken(), 0);
            return true;
        }
        return false;
    }


    @Override
    public void updateSpeechResult(String result) {

        editText_productName.setText(result);
        dismissDialog();
    }

    @Override
    public void updateSpeechPartialResult(String result) {

    }

    @Override
    public void dismissDialog() {

        if (speechToVoiceDialog != null && speechToVoiceDialog.isVisible())
            speechToVoiceDialog.dismiss();
    }

    public void setProductNameOnBar(String text){

        textView_productName.setText(text);

        if (viewFlipper.getDisplayedChild() != 0) {
            viewFlipper.showPrevious();

        }
    }

}
