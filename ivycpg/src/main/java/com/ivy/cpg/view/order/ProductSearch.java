package com.ivy.cpg.view.order;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Vector;

public class ProductSearch implements View.OnClickListener,TextView.OnEditorActionListener,SpeechResultListener {

    private Context context;
    private EditText mEdt_searchproductName;
    private Vector<ProductMasterBO> masterList,searchedList;
    private BusinessModel bModel;
    private SearchAsync searchAsync;
    private String selectedSpecialFilter;
    private String userEnteredText;
    private static ProductSearch instance=null;
    private ProductSearchCallBack productSearchCallBack;
    public static String SCREEN_CODE_ORDER="ORDER";
    private String current_screen_code;

    private static final String ALL = "ALL";

    private ProductHelper productHelper;

    private int productId;
    private ArrayList<Integer> attributeProductIds;

    private final String FILTER_CODE_SBD = "Filt02";
    private final String FILTER_CODE_SBD_GAPS = "Filt03";
    public final String mOrdered = "Filt04";
    private final String mPurchased = "Filt05";
    private final String mInitiative = "Filt06";
    private final String mOnAllocation = "Filt07";
    private final String mInStock = "Filt08";
    private final String mPromo = "Filt09";
    private final String mMustSell = "Filt10";
    private final String mFocusBrand = "Filt11";
    private final String mFocusBrand2 = "Filt12";
    private final String msih = "Filt13";
    private final String mOOS = "Filt14";
    private final String mNMustSell = "Filt16";
    private final String mStock = "Filt17";
    private final String mDiscount = "Filt18";
    public final String mSuggestedOrder = "Filt25";
    private final String mDeadProducts = "Filt15";
    private final String mCommon = "Filt01";
    private final String mCompertior = "Filt23";
    private final String mFocusBrand3 = "Filt20";
    private final String mDrugProducts = "Filt28";
    private final String mFocusBrand4 = "Filt21";
    private final String mSMP = "Filt22";
    private final String mNearExpiryTag = "Filt19";
    private final String mShelf = "Filt24";

    private final String GENERAL = "General";

    private SpeechToVoiceDialog speechToVoiceDialog;
    private ViewFlipper viewFlipper;
    private Button mBtn_Search;
    private Button mBtn_clear;

    private TextView textView_productName;

    private boolean isSpecialFilter,isProductFilter,isUserEntryFilter;
    public ProductSearch(Context context,Vector<ProductMasterBO> masterList,BusinessModel bModel,String current_screen_code){
        this.context=context;
        this.masterList=masterList;
        this.bModel=bModel;
        this.current_screen_code=current_screen_code;
        productHelper=ProductHelper.getInstance(context);
        if (productSearchCallBack == null) {
            if (context instanceof ProductSearchCallBack) {
                this.productSearchCallBack = (ProductSearchCallBack) context;
            }
        }

        textView_productName = context.findViewById(R.id.productName);
        textView_productName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
        mEdt_searchproductName =  context.findViewById(R.id.edt_searchproductName);
        mBtn_Search =  context.findViewById(R.id.btn_search);
        viewFlipper =  context.findViewById(R.id.view_flipper);
        mBtn_clear =  context.findViewById(R.id.btn_clear);

        if (bModel.configurationMasterHelper.IS_VOICE_TO_TEXT == -1)
            context.findViewById(R.id.btn_speech).setVisibility(View.GONE);

        mBtn_Search.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);
        mEdt_searchproductName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));


        mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {

                    startSearch(masterList,mEdt_searchproductName.getText().toString());
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

        context.findViewById(R.id.btn_speech).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int permissionStatus = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {


                    speechToVoiceDialog = new SpeechToVoiceDialog();
                    speechToVoiceDialog.setCancelable(true);
                    speechToVoiceDialog.show(context.getSupportFragmentManager(), "SPEECH_TO_TEXT");

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

      public static ProductSearch getInstance(Context context,Vector<ProductMasterBO> masterList,BusinessModel bModel,String current_screen_code) {

        if (instance == null) {
            instance = new ProductSearch(context,masterList,bModel,current_screen_code);
        }
        return instance;
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

            if (bModel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
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

    public boolean isSpecialFilterAppliedProduct(String selectedFilter, ProductMasterBO ret) {

        final String GENERAL = "General";

        return selectedFilter.equalsIgnoreCase(GENERAL)
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_SBD) && ret.isRPS())
                || (selectedFilter.equalsIgnoreCase(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (selectedFilter.equalsIgnoreCase(mPurchased) && ret.getIsPurchased() == 1)
                || (selectedFilter.equalsIgnoreCase(mInitiative) && ret.getIsInitiativeProduct() == 1)
                || (selectedFilter.equalsIgnoreCase(mCommon) && applyCommonFilterConfig(ret))
                || (selectedFilter.equalsIgnoreCase(FILTER_CODE_SBD_GAPS) && (ret.isRPS() && !ret.isSBDAcheived()))
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

                return (bo.getConfigCode().equals(FILTER_CODE_SBD) && ret.isRPS()) || (bo.getConfigCode().equals(FILTER_CODE_SBD_GAPS) && ret.isRPS() && !ret.isSBDAcheived()) || (bo.getConfigCode().equals(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
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
            Vector<ConfigureBO> specialFilterList = bModel.configurationMasterHelper
                    .getGenFilter();
            for (int i = 0; i < specialFilterList.size(); i++) {
                if (specialFilterList.get(i).getHasLink() == 1) {
                    if (!bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        defaultFilter = specialFilterList.get(i).getConfigCode();
                        break;
                    } else {
                        if (bModel.getRetailerMasterBO().getIsVansales() == 1) {
                            if (specialFilterList.get(i).getConfigCode().equals(msih)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
                                break;
                            } else if (!specialFilterList.get(i).getConfigCode().equals(mInStock)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
                                break;
                            }
                        } else {
                            if (specialFilterList.get(i).getConfigCode().equals(mInStock)) {
                                defaultFilter = specialFilterList.get(i).getConfigCode();
                                break;
                            } else if (!specialFilterList.get(i).getConfigCode().equals(msih)) {
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
            mEdt_searchproductName.requestFocus();
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(mEdt_searchproductName, InputMethodManager.SHOW_FORCED);
            } catch (Exception e) {
                Commons.printException(e);
            }

        } else if (vw == mBtn_clear) {
            viewFlipper.showPrevious();
            mEdt_searchproductName.setText("");
            //productName.setText("");
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }

            startSearch(masterList,"");

        }
    }

    public void hideSoftInputFromWindow(){
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                mEdt_searchproductName.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (mEdt_searchproductName.getText().length() >= 3) {

                startSearch(masterList,mEdt_searchproductName.getText().toString());
            } else {
                Toast.makeText(context, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
            InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
            return true;
        }
        return false;
    }


    @Override
    public void updateSpeechResult(String result) {

        mEdt_searchproductName.setText(result);
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
