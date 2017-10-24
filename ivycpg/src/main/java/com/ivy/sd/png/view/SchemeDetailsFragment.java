package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SchemeDetailsFragment extends IvyBaseFragment {

    private BusinessModel bmodel;

    private LinearLayout mainLayout;
    private LinearLayout.LayoutParams linearlprams, linearlpramsHSL, linearlpramsVSL,
            linearlpramsSub1, linearlpramsSub2, linearlpramsSub3,
            linearlpramsSub4;
    private LinearLayout.LayoutParams lprams;
    private Set<SchemeBO> uniqueSchemes;
    private List<TempSchemeBO> selectAllList;
    private LinearLayout linearWidgetSchemeReport;
    private Context ctxt;


    private LinearLayout mAddViewLayout;
    private LinearLayout mAddViewHorizontalLayout;
    private static final String PIECE = "Pcs";
    private static final String CASES = "Cases";
    private static final String OUTER = "Outer";
    private String rupeesLabel = "Rs";
    private static final String CURRENCT_LABEL = "currency";
    private static final String SCHEME_BUY_TYPE_QTY = "QTY";
    private static final String SCHEME_BUY_TYPE_VALUE = "SV";
    private static final String AND_LOGIC = "AND";
    private static final String ANY_LOGIC = "ANY";
    private static final String ONLY_LOGIC = "ONLY";
    private String mProductID = "";
    // scheme dialog product width and height
    private int mProductNameWidth = 0;
    private int mSchemeDetailWidth = 0;
    private int mTotalScreenWidth = 0;
    private int mTextViewHeight = 0;
    private int mTextViewSize = 14;
    private int mSlabWiseProductNameWidth = 0;
    private int mSlabWiseSchemeNameWidth = 0;

    private List<SchemeBO> schemes;
    private String pdname;
    private String prodId;
    private ProductMasterBO productObj;
    private int flag;
    private View rootView;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        this.prodId = bmodel.productHelper.getProdId();
        this.mProductID = bmodel.productHelper.getProdId();
        this.mTotalScreenWidth = bmodel.productHelper.getTotalScreenSize();
        ;

        ctxt = getActivity().getApplicationContext();
        this.schemes = bmodel.productHelper.getSchemes();
        this.productObj = bmodel.productHelper.getProductObj();
        this.flag = bmodel.productHelper.getFlag();
        this.pdname = bmodel.productHelper.getPdname();

        if (mTotalScreenWidth > 1000) {
            mTextViewSize = mTextViewSize - 2;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getActivity().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.fragment_scheme_details, container, false);

        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.productHelper.getPdname());


        if (bmodel.labelsMasterHelper.applyLabels(CURRENCT_LABEL) != null) {
            rupeesLabel = bmodel.labelsMasterHelper
                    .applyLabels(CURRENCT_LABEL);
        }
        TextView schemeTitleTV = (TextView) rootView.findViewById(R.id.scheme_info_title);
        mainLayout = (LinearLayout) rootView.findViewById(R.id.schemeDialogwidget);
        linearlpramsSub3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearWidgetSchemeReport = new LinearLayout(bmodel.getActivity());
        linearWidgetSchemeReport.setLayoutParams(linearlpramsSub3);
        linearWidgetSchemeReport
                .setBackgroundColor(Color.parseColor("#9CE7F9"));
        linearWidgetSchemeReport.setOrientation(LinearLayout.VERTICAL);
        linearlprams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearlpramsSub1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearlpramsSub1.setMargins(3, 3, 3, 3);
        linearlpramsSub2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearlpramsSub2.setMargins(1, 1, 1, 1);

        linearlpramsSub4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearlpramsSub4.setMargins(3, 3, 3, 10);

        linearlpramsHSL = new LinearLayout.LayoutParams(1,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearlpramsHSL.setMargins(0, 2, 0, 2);
        linearlpramsHSL.setMargins(1, 0, 1, 0);
        linearlpramsVSL = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        linearlpramsVSL.setMargins(2, 0, 2, 0);
        lprams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lprams.weight = 1;
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);

        Spinner schemeSpinner = (Spinner) rootView.findViewById(R.id.scheme_spnr);
        if (flag == 1) {

            TextView productTitleTV = (TextView) rootView.findViewById(R.id.product_info_title);

            productTitleTV.setText(pdname);
            boolean isSchemeAvailable = false;
            if (schemes != null && schemes.size() > 0) {
                for (SchemeBO scbo : schemes) {
                    for (SchemeProductBO isSchemeHavingProd : scbo
                            .getBuyingProducts()) {
                        if (isSchemeHavingProd.getProductId().equals(prodId))
                            isSchemeAvailable = true;
                    }
                }
            }
            schemeTitleTV.setText(pdname);
            if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                if (schemes != null && schemes.size() > 0 && isSchemeAvailable) {
                    //tabHost.addTab(setContent1);
                    schemeTitleTV.setWidth(outMetrics.widthPixels);
                    if (uniqueSchemes != null && !uniqueSchemes.isEmpty())
                        uniqueSchemes.clear();
                    updateSchemeView(mProductID);
                }
            } else if (bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
                if (schemes != null && schemes.size() > 0 && isSchemeAvailable) {
                    schemeTitleTV.setWidth(outMetrics.widthPixels);
                    updateSchemeView(mProductID);
                }
            }
        } else if (flag == 0) {
            schemeTitleTV.setVisibility(View.GONE);
            LinearLayout temp = (LinearLayout) rootView.findViewById(R.id.scheme_info_layout);
            temp.setVisibility(View.GONE);
            schemeSpinner.setVisibility(View.VISIBLE);
            LinearLayout lr = (LinearLayout) rootView.findViewById(R.id.spinner_tab_widget);
            android.view.ViewGroup.LayoutParams lp = lr.getLayoutParams();
            lp.width = outMetrics.widthPixels;
            lr.setLayoutParams(lp);

            // schemeTitleTV.setWidth(outMetrics.widthPixels);
            ArrayAdapter<SpinnerBO> schemeAdapter = new ArrayAdapter<SpinnerBO>(ctxt,
                    android.R.layout.simple_spinner_item);
            schemeAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            schemeAdapter.add(new SpinnerBO(0, ctxt.getResources()
                    .getString(R.string.select)));
            schemeAdapter.add(new SpinnerBO(1, ctxt.getResources()
                    .getString(R.string.all)));
            for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {
                if (bo.isPromo())
                    schemeAdapter.add(new SpinnerBO(Integer.parseInt(bo
                            .getProductID()), bo.getProductName()));
            }
            schemeSpinner.setAdapter(schemeAdapter);
        }

        rootView.findViewById(R.id.scheme_tab_widget).setVisibility(
                View.VISIBLE);

        return rootView;
    }


    public void onClick(View v) {
    }

    class TempSchemeBO {
        private int parentId;
        private String parentDesc;
        private String type;
        List<TempSlabBO> slabList;

        public int getParentId() {
            return parentId;
        }

        public void setParentId(int parentId) {
            this.parentId = parentId;
        }

        public String getParentDesc() {
            return parentDesc;
        }

        public void setParentDesc(String parentDesc) {
            this.parentDesc = parentDesc;
        }

        public List<TempSlabBO> getSlabList() {
            return slabList;
        }

        public void setSlabList(List<TempSlabBO> slabList) {
            this.slabList = slabList;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    class TempSlabBO {
        private String slabId;
        private String SlabDescription;
        List<TempBuyProductBO> buyproductList;
        private int buyQty;

        public String getSlabId() {
            return slabId;
        }

        public void setSlabId(String slabId) {
            this.slabId = slabId;
        }

        public String getSlabDescription() {
            return SlabDescription;
        }

        public void setSlabDescription(String slabDescription) {
            SlabDescription = slabDescription;
        }

        public List<TempBuyProductBO> getBuyproductList() {
            return buyproductList;
        }

        public void setBuyproductList(List<TempBuyProductBO> buyproductList) {
            this.buyproductList = buyproductList;
        }

        public int getBuyQty() {
            return buyQty;
        }

        public void setBuyQty(int buyQty) {
            this.buyQty = buyQty;
        }
    }

    class TempBuyProductBO {
        private String buyproductId;
        private String buyproductDescription;
        private int applycount;
        private String buyType;
        private String GroupName;
        private String GroupType;
        private int isCombination;
        private String uomDescription;
        private String buyQty;
        private int freeQtyMin, freeQtyMax;

        public String getBuyQty() {
            return buyQty;
        }

        public String getBuyproductId() {
            return buyproductId;
        }

        public void setBuyproductId(String buyproductId) {
            this.buyproductId = buyproductId;
        }

        public String getBuyproductDescription() {
            return buyproductDescription;
        }

        public void setBuyproductDescription(String buyproductDescription) {
            this.buyproductDescription = buyproductDescription;
        }

        public int getApplycount() {
            return applycount;
        }

        public void setApplycount(int applycount) {
            this.applycount = applycount;
        }

        public String getBuyType() {
            return buyType;
        }

        public void setBuyType(String buyType) {
            this.buyType = buyType;
        }

        public String getGroupName() {
            return GroupName;
        }

        public void setGroupName(String groupName) {
            GroupName = groupName;
        }

        public String getGroupType() {
            return GroupType;
        }

        public void setGroupType(String groupType) {
            GroupType = groupType;
        }

        public int getIsCombination() {
            return isCombination;
        }

        public void setIsCombination(int isCombination) {
            this.isCombination = isCombination;
        }

        public String getUomDescription() {
            return uomDescription;
        }

        public void setUomDescription(String uomDescription) {
            this.uomDescription = uomDescription;
        }

        public int getFreeQtyMin() {
            return freeQtyMin;
        }

        public void setFreeQtyMin(int freeQtyMin) {
            this.freeQtyMin = freeQtyMin;
        }

        public int getFreeQtyMax() {
            return freeQtyMax;
        }

        public void setFreeQtyMax(int freeQtyMax) {
            this.freeQtyMax = freeQtyMax;
        }

    }


    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    public int convertSpToPixels(float sp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }
    ///// Newly added by rajesh.k

    private void updateSchemeView(String productid) {
        ArrayList<Integer> parentIdList = bmodel.schemeDetailsMasterHelper.getParentIdListByProductId().get(productid);
        if (parentIdList != null) {
            for (Integer parentId : parentIdList) {
                boolean isSameGroupAvailable = bmodel.schemeDetailsMasterHelper.isSameGroupAvailableinDifferentSlab(parentId);

                final ArrayList<String> schemeIdList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(parentId);
                /*if (schemeIdList.size() == 1) {
                    mSchemeDetailWidth = (mTotalScreenWidth) / 4 - 20;
                    mProductNameWidth = mTotalScreenWidth - mSchemeDetailWidth - 40;


                } else {
                    mProductNameWidth = 350;
                    mSchemeDetailWidth = 100;
                }*/

                //int prodWidth = getResources().getInteger(R.integer.product_width);
                //int slabWidth = getResources().getInteger(R.integer.slab_width);
                int prodWidth = ((mTotalScreenWidth) * 40) / 100;
                int slabWidth = ((mTotalScreenWidth) * 30) / 100;

                mProductNameWidth = prodWidth;
                mSchemeDetailWidth = slabWidth;

                mSlabWiseSchemeNameWidth = (mTotalScreenWidth) / 4 - 20;
                mSlabWiseProductNameWidth = mTotalScreenWidth - mSlabWiseSchemeNameWidth - 40;


                SchemeBO schemeBO = null;

                if (schemeIdList != null)
                    schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeIdList.get(0));

                if (schemeBO != null) {
                    LinearLayout.LayoutParams layoutParamsSchemeTitile = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                    //layoutParamsSchemeTitile.weight=0.1f;
                    layoutParamsSchemeTitile.setMargins(0, 20, 0, 10);
                    layoutParamsSchemeTitile.gravity = Gravity.LEFT | Gravity.CENTER;
                    TextView schemeTitleTV = getTextViewTitle(false, Gravity.LEFT, true);
                    schemeTitleTV.setText(schemeBO.getProductName());
                    schemeTitleTV.setTextColor(getResources().getColor(R.color.FullBlack));
                    schemeTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                    //schemeTitleTV.setTextSize(((mTotalScreenWidth*2)/100));
                    schemeTitleTV.setTextSize(mTextViewSize);
                    schemeTitleTV.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    schemeTitleTV.setLayoutParams(layoutParamsSchemeTitile);
                    mainLayout.addView(schemeTitleTV);
                    final List<SchemeProductBO> schemeBuyList = schemeBO.getBuyingProducts();
                    List<SchemeProductBO> schemeFreeList = schemeBO.getFreeProducts();


                    ArrayList<String> groupName = new ArrayList<>();
                    if (schemeBuyList != null && schemeBuyList.size() > 0) {

                        LinearLayout.LayoutParams buyParentLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (mTotalScreenWidth * 6) / 100);//ViewGroup.LayoutParams.WRAP_CONTENT);
                        LinearLayout buyParent = new LinearLayout(ctxt);
                        buyParent.setLayoutParams(buyParentLayout);
                        buyParent.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                        //buyParent.setAlpha(0.2f);

                        TextView buyTitleTV = getTextViewTitle(true, Gravity.LEFT, false);
                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (mTotalScreenWidth * 6) / 100);//ViewGroup.LayoutParams.WRAP_CONTENT);
                        //layoutParams1.weight=0.1f;
                        layoutParams1.setMargins(0, 10, 0, 10);
                        layoutParams1.gravity = Gravity.CENTER_VERTICAL;
                        buyTitleTV.setLayoutParams(layoutParams1);
                        buyTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                        //buyTitleTV.setTextSize(((mTotalScreenWidth*2)/100)-2);
                        //float size1 =convertSpToPixels(getResources().getDimension(R.dimen.dimens_font_12dp),getActivity());
                        //buyTitleTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.dimens_font_12dp));
                        buyTitleTV.setTextSize(mTextViewSize);
                        buyTitleTV.setText("BUY");
                        buyTitleTV.setWidth(mProductNameWidth);
                        buyTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        buyTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);
                        //buyTitleTV.setWidth(150);
                        buyTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        //mainLayout.addView(buyTitleTV);

                        LinearLayout buyLayout = null;
                        buyLayout = new LinearLayout(ctxt);
                        buyLayout.setOrientation(LinearLayout.HORIZONTAL);
                        buyLayout.setLayoutParams(layoutParams1);
                        buyLayout.addView(buyTitleTV);

                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (mTotalScreenWidth * 6) / 100);//ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams2.gravity = Gravity.CENTER_VERTICAL;


                        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(parentId);
                        if (schemeList != null) {
                            for (int k = schemeList.size() - 1; k >= 0; k--) {

                                TextView slab = getTextViewTitle(true, Gravity.CENTER, false);
                                slab.setLayoutParams(layoutParams2);
                                slab.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                                //slab.setTextSize(((mTotalScreenWidth*2)/100)-2);
                                slab.setTextSize(mTextViewSize);
                                if (bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(k)).getScheme() != null)
                                    slab.setText(bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(k)).getScheme());
                                slab.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                                slab.setWidth(mSchemeDetailWidth);
                                //buyTitleTV.setWidth(150);
                                slab.setTextColor(getResources().getColor(R.color.FullBlack));
                                buyLayout.addView(slab);
                            }
                        }

                        buyParent.addView(buyLayout);
                        mainLayout.addView(buyParent);

                        int i = 0;
                        for (SchemeProductBO buyProductBO : schemeBuyList) {
                            if (!groupName.contains(buyProductBO.getGroupName())) {
                                i = i + 1;
                                if (i > 1) {
                                    TextView groupLogicType = getTextViewTitle(false, Gravity.LEFT, false);
                                    groupLogicType.setTextColor(getResources().getColor(R.color.FullBlack));

                                    if (schemeBO.getType().equalsIgnoreCase("AND")) {
                                        groupLogicType.setText("&");
                                    } else if (schemeBO.getType().equalsIgnoreCase("ANY")) {
                                        groupLogicType.setText("OR");
                                    } else {
                                        groupLogicType.setText(schemeBO.getType());
                                    }

                                    groupLogicType.setTypeface(null, Typeface.ITALIC);
                                    mainLayout.addView(groupLogicType);
                                }

                                if (buyProductBO.getGroupBuyType().equals(AND_LOGIC)) {
                                    final HorizontalScrollView ll = addViewAndLogicBuyNew(parentId, buyProductBO.getGroupName());
                                    mainLayout.addView(ll);

                                } else if (buyProductBO.getGroupBuyType().equals(ANY_LOGIC)) {
                                    final HorizontalScrollView ll = addViewANYLogicBUY(parentId, buyProductBO.getGroupName());
                                    mainLayout.addView(ll);

                                } else if (buyProductBO.getGroupBuyType().equals(ONLY_LOGIC)) {
                                    final HorizontalScrollView ll = addViewONLYLogicBuy(parentId, buyProductBO.getGroupName());
                                    mainLayout.addView(ll);
                                }


                            }

                            groupName.add(buyProductBO.getGroupName());

                        }
                    }
                    boolean isAlreadyAddedFreeProduct = false;
                    boolean isAlreadyAddedOtherDisc = false;

                    if (!isSameGroupAvailable) {
                        TextView freeTitleTV = getTextViewTitle(true, Gravity.LEFT, true);
                        freeTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                        freeTitleTV.setTextColor(getResources().getColor(R.color.FullBlack));
                        //freeTitleTV.setTextSize(((mTotalScreenWidth*2)/100)-2);
                        freeTitleTV.setTextSize(mTextViewSize);
                        freeTitleTV.setText("GET FreeProduct");
                        freeTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        freeTitleTV.setWidth(150);

                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (mTotalScreenWidth * 6) / 100);//ViewGroup.LayoutParams.WRAP_CONTENT);

                        layoutParams1.setMargins(0, 10, 0, 10);
                        freeTitleTV.setLayoutParams(layoutParams1);
                        freeTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        freeTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);
                        mainLayout.addView(freeTitleTV);
                    }


                    for (String schemeId : schemeIdList) {

                        final SchemeBO schemeBO1 = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeId);

                        schemeFreeList = schemeBO1.getFreeProducts();

                        if (isSameGroupAvailable) {
                            if (schemeFreeList != null && schemeFreeList.size() > 0 && !isAlreadyAddedFreeProduct) {

                                int i = 0;
                                for (SchemeProductBO freeProductBO : schemeFreeList) {
                                    if (freeProductBO.getGroupName() != null) {
                                        if (!groupName.contains(schemeId + freeProductBO.getGroupName())) {
                                            if (i == 0) {
                                                isAlreadyAddedFreeProduct = true;
                                                TextView freeTitleTV = getTextViewTitle(true, Gravity.LEFT, true);
                                                freeTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                                                freeTitleTV.setTextColor(getResources().getColor(R.color.FullBlack));
                                                //freeTitleTV.setTextSize(((mTotalScreenWidth*2)/100)-2);
                                                freeTitleTV.setTextSize(mTextViewSize);
                                                freeTitleTV.setText("GET FreeProduct");
                                                freeTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);
                                                freeTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                                                freeTitleTV.setWidth(150);

                                                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (mTotalScreenWidth * 6) / 100);//ViewGroup.LayoutParams.WRAP_CONTENT);

                                                layoutParams1.setMargins(0, 20, 0, 10);
                                                freeTitleTV.setLayoutParams(layoutParams1);
                                                freeTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                                mainLayout.addView(freeTitleTV);
                                            }
                                            i++;
                                            if (i > 1) {
                                                TextView groupLogicType = getTextViewTitle(false, Gravity.LEFT, false);

                                                //groupLogicType.setText(schemeBO.getFreeType());
                                                groupLogicType.setTextColor(getResources().getColor(R.color.FullBlack));

                                                if (schemeBO.getType().equalsIgnoreCase("AND")) {
                                                    groupLogicType.setText("&");
                                                } else if (schemeBO.getType().equalsIgnoreCase("ANY")) {
                                                    groupLogicType.setText("OR");
                                                } else {
                                                    groupLogicType.setText(schemeBO.getType());
                                                }

                                                groupLogicType.setTypeface(null, Typeface.ITALIC);
                                                mainLayout.addView(groupLogicType);
                                            }
                                            if (freeProductBO.getGroupBuyType().equals(AND_LOGIC)) {
                                                final HorizontalScrollView ll = addViewANDLogicGET(parentId, freeProductBO.getGroupName());
                                                mainLayout.addView(ll);
                                            } else if (freeProductBO.getGroupBuyType().equals(ANY_LOGIC)) {
                                                final HorizontalScrollView ll = addViewANYLogicGET(parentId, freeProductBO.getGroupName());
                                                mainLayout.addView(ll);
                                            } else if (freeProductBO.getGroupBuyType().equals(ONLY_LOGIC)) {

                                                final HorizontalScrollView ll = addViewONLYLogicGET(parentId, freeProductBO.getGroupName());
                                                mainLayout.addView(ll);

                                            }
                                        }
                                        groupName.add(schemeId + freeProductBO.getGroupName());
                                    }
                                }

                            }
                        } else {
                            TextView slabNameTV = getNameTv();
                            slabNameTV.setText(schemeBO1.getScheme());
                            mainLayout.addView(slabNameTV);

                            ArrayList<String> groupNameList = bmodel.schemeDetailsMasterHelper.getFreeProductBuyNameListBySchemeID().get(schemeId);
                            if (groupNameList != null) {
                                int j = 1;
                                for (String grpName : groupNameList) {
                                    String groupLogic = bmodel.schemeDetailsMasterHelper.getGroupBuyTypeByGroupName().get(schemeId + grpName);

                                    if (groupLogic != null) {
                                        if (j > 1) {
                                            TextView groupLogicType = getTextViewTitle(false, Gravity.CENTER, false);

                                            groupLogicType.setText(schemeBO1.getFreeType());
                                            groupLogicType.setTypeface(null, Typeface.ITALIC);
                                            mainLayout.addView(groupLogicType);
                                        }

                                        if (groupLogic.equals(AND_LOGIC)) {
                                            final LinearLayout ll = addSlabWiseFreeProductANDLogic(schemeId, grpName);
                                            mainLayout.addView(ll);

                                        } else if (groupLogic.equals(ANY_LOGIC)) {
                                            final LinearLayout ll = addSlabWiseFreeProductANYLogic(schemeId, grpName);
                                            mainLayout.addView(ll);
                                        } else if (groupLogic.equals(ONLY_LOGIC)) {
                                            final LinearLayout ll = addSlabWiseFreeProductONLYLogic(schemeId, grpName);
                                            mainLayout.addView(ll);
                                        }

                                    }
                                    j++;

                                }


                            }

                        }
                        //mainLayout.addView(getHorizontalLine());
                        if (schemeFreeList != null && schemeFreeList.size() > 0 && !isAlreadyAddedOtherDisc) {
                            SchemeProductBO freeProductBO = schemeFreeList.get(0);
                            if (freeProductBO != null && (freeProductBO.getMaxPercent() > 0 || freeProductBO.getMaxAmount() > 0 || freeProductBO.getPriceMaximum() > 0)) {
                                TextView freeTitleTV = getTextViewTitle(true, Gravity.LEFT, true);
                                freeTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                                //freeTitleTV.setTextSize(((mTotalScreenWidth*2)/100)-2);
                                freeTitleTV.setTextSize(mTextViewSize);
                                freeTitleTV.setText("GET Discount");
                                freeTitleTV.setWidth(150);
                                freeTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (mTotalScreenWidth * 6) / 100);//ViewGroup.LayoutParams.WRAP_CONTENT);

                                layoutParams1.setMargins(0, 20, 0, 10);
                                freeTitleTV.setLayoutParams(layoutParams1);
                                freeTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                freeTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);
                                mainLayout.addView(freeTitleTV);

                                isAlreadyAddedOtherDisc = true;

                                final HorizontalScrollView ll = addViewSchemeFreeeLogicNew(parentId);
                                mainLayout.addView(ll);


                            }
                        }

                    }

                    LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30);
                    lineLayoutParams.setMargins(0, 5, 0, 5);
                    lineLayoutParams.gravity = Gravity.CENTER;
                    LinearLayout lineLayouts = new LinearLayout(ctxt);
                    lineLayouts.setGravity(Gravity.CENTER);

                    lineLayouts.setLayoutParams(lineLayoutParams);
                    lineLayouts.addView(getHorizontalLine());
                    mainLayout.setGravity(Gravity.CENTER);
                    mainLayout.addView(lineLayouts);
                }
            }
        }


    }


    private HorizontalScrollView addViewANYLogicBUY(int schemeParentId, String groupName) {
        final int maximumLineCount = bmodel.schemeDetailsMasterHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.setMargins(0, 5, 0, 0);

        mAddViewLayout = new LinearLayout(ctxt);

        layoutParams1.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();

        if (size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(ctxt);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            if (bmodel.configurationMasterHelper.IS_SCHEME_SLAB_ON)
                txt.setText("SLAB");
            else
                txt.setText("SKU");
            txt.setMaxLines(maximumLineCount);


            txt.setHeight(maximumLineCount * mTextViewHeight);


            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getBuyingProducts();


                //childHeaderView.addView(getHorizontalLine());
                if (bmodel.configurationMasterHelper.IS_SCHEME_SLAB_ON) {
                    TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                    productTV.setText(R.string.qty);
                    productTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    productTV.setTextColor(getResources().getColor(R.color.FullBlack));
                    productTV.setWidth(mProductNameWidth);
                    childHeaderView.addView(productTV);
                } else {
                    for (SchemeProductBO schemeProductBO : buyProdList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            if (productBO != null) {
                                TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                                productTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                productTV.setTextColor(getResources().getColor(R.color.FullBlack));
                                productTV.setText(productBO.getProductShortName());
                                productTV.setWidth(mProductNameWidth);
                                childHeaderView.addView(productTV);
                            }
                        }
                    }
                }
            }

            mAddViewLayout.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));

                if (schemeBO != null) {
                    TextView schemeNameTV = getNameTv();
                    schemeNameTV.setText(schemeBO.getScheme());

                    schemeNameTV.setWidth(mSchemeDetailWidth);

                    schemeNameTV.setMaxLines(maximumLineCount);
                    schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);

                    //schemeNameTV.setSingleLine(true);
                    LinearLayout schemeChildView = new LinearLayout(ctxt);
                    schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    schemeChildView.setOrientation(LinearLayout.VERTICAL);
                    schemeChildView.addView(schemeNameTV);
                    List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();
                    for (SchemeProductBO schemeProductBO : buyProductList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {
                            TextView tv = getNameTv();
                            tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                            tv.setTextColor(getResources().getColor(R.color.FullBlack));
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            if (productBO != null) {
                                if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                    if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + schemeProductBO.getUomDescription());
                                    } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + schemeProductBO.getUomDescription());
                                    } else {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + schemeProductBO.getUomDescription());
                                    }
                                } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                    tv.setText(schemeProductBO.getBuyQty() + "-" + schemeProductBO.getTobuyQty() + " " + rupeesLabel);
                                }
                                tv.setWidth(mSchemeDetailWidth);

                                schemeChildView.addView(tv);
                                break;
                            }

                        }


                    }
                    mAddViewLayout.addView(schemeChildView);


                }
            }


        }
        horizontalScrollView.addView(mAddViewLayout);


        return horizontalScrollView;
    }

    private HorizontalScrollView addViewONLYLogicBuy(int schemeParentId, String groupName) {
        int maximumLineCount = bmodel.schemeDetailsMasterHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        if (maximumLineCount == 0)
            maximumLineCount = 1;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.setMargins(0, 5, 0, 0);

        mAddViewLayout = new LinearLayout(ctxt);

        layoutParams1.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(ctxt);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getBuyingProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                        productTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        productTV.setTextColor(Color.BLACK);
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setText(productBO.getProductShortName());
                        }
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(productTV);
                    }
                }
            }


            mAddViewLayout.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));

                LinearLayout.LayoutParams detailLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                detailLayout.weight = 0.1f;
                //layoutParams2.setMargins(0, 10, 0, 10);
                detailLayout.gravity = Gravity.CENTER_VERTICAL;

                if (schemeBO != null) {
                    TextView schemeNameTV = getNameTv();
                    schemeNameTV.setText(schemeBO.getScheme());
                    schemeNameTV.setWidth(mSchemeDetailWidth);
                    schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);
                    LinearLayout schemeChildView = new LinearLayout(ctxt);
                    schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    schemeChildView.setOrientation(LinearLayout.VERTICAL);
                    schemeChildView.addView(schemeNameTV);
                    List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();
                    for (SchemeProductBO schemeProductBO : buyProductList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {
                            TextView tv = getNameTv();
                            tv.setTextColor(Color.BLACK);
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            if (productBO != null) {
                                if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                    if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + schemeProductBO.getUomDescription());
                                    } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + schemeProductBO.getUomDescription());
                                    } else {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + schemeProductBO.getUomDescription());
                                    }
                                } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                    tv.setText(schemeProductBO.getBuyQty() + "-" + schemeProductBO.getTobuyQty() + " " + rupeesLabel);
                                }
                                tv.setWidth(mSchemeDetailWidth);
                                tv.setSingleLine(true);


                            }
                            tv.setLayoutParams(detailLayout);
                            schemeChildView.addView(tv);
                        }
                    }
                    mAddViewLayout.addView(schemeChildView);


                }
            }


        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }

    private HorizontalScrollView addViewANDLogicGET(int schemeParentId, String groupName) {
        final int maximumLineCount = bmodel.schemeDetailsMasterHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAddViewLayout = new LinearLayout(ctxt);
        layoutParams1.bottomMargin = 10;
        //layoutParams1.setMargins(0,10,0,10);
        layoutParams1.gravity = Gravity.LEFT | Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(ctxt);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                        productTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setText(productBO.getProductShortName());
                            productTV.setWidth(mProductNameWidth);
                            childHeaderView.addView(productTV);
                        }

                    }
                }
            }


            mAddViewLayout.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));

                if (schemeBO != null) {
                    List<SchemeProductBO> buyProductList = schemeBO.getFreeProducts();
                    if (buyProductList != null && buyProductList.size() > 0) {
                        SchemeProductBO freeProductBO = buyProductList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {
                            LinearLayout schemeChildView = new LinearLayout(ctxt);
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            TextView schemeNameTV = getNameTv();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : buyProductList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {
                                    TextView tv = getNameTv();
                                    tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    if (productBO != null) {

                                        if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        } else {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        }
                                        tv.setWidth(mSchemeDetailWidth);
                                        tv.setSingleLine(true);

                                        schemeChildView.addView(tv);
                                    }

                                }
                            }
                            mAddViewLayout.addView(schemeChildView);


                        }
                    }
                }
            }


        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }


    private HorizontalScrollView addViewAndLogicBuyNew(int parentId, String groupName) {

        final int maximumLineCount = bmodel.schemeDetailsMasterHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, parentId);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.setMargins(0, 5, 0, 0);
        mAddViewLayout = new LinearLayout(ctxt);

        layoutParams1.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }

        final ArrayList<String> productIdList = bmodel.schemeDetailsMasterHelper.getProductIdListByParentId().get(parentId);
        if (productIdList != null) {

            final ArrayList<String> schemeIdList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(parentId);
            int size = schemeIdList.size();
            int j = 0;

            for (String productId : productIdList) {
                LinearLayout headerLayout = new LinearLayout(ctxt);
                headerLayout.setOrientation(LinearLayout.HORIZONTAL);
                headerLayout.setLayoutParams(layoutParams1);
                mAddViewHorizontalLayout = new LinearLayout(ctxt);
                mAddViewHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                mAddViewHorizontalLayout.setLayoutParams(layoutParams1);
                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(productId);
                if (productBO != null) {
                    if (j == 0) {
                        TextView headerTV = getTextViewTitle(false, Gravity.CENTER, false);
                        headerTV.setText("SKU");
                        headerTV.setWidth(mProductNameWidth);
                        headerTV.setHeight(maximumLineCount * mTextViewHeight);
                        headerLayout.addView(headerTV);
                    }

                    for (int i = size - 1; i >= 0; i--) {

                        String schemeid = schemeIdList.get(i);
                        SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeid);
                        if (j == 0) {

                            if (schemeBO != null) {
                                TextView schemeNameTV = getNameTv();
                                schemeNameTV.setText(schemeBO.getScheme());
                                schemeNameTV.setWidth(mSchemeDetailWidth);
                                schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);
                                //headerLayout.addView(getVerticalLine());
                                headerLayout.addView(schemeNameTV);

                            }
                        }


                        final SchemeProductBO buyProductBO = bmodel.schemeDetailsMasterHelper.getBuyProductBOBySchemeidWithPid().get(schemeid + productId);
                        if (buyProductBO != null && buyProductBO.getGroupName().equals(groupName)) {
                            if (i == size - 1) {
                                TextView produtNameTV = getTextViewTitle(false, Gravity.LEFT, false);
                                produtNameTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                produtNameTV.setTextColor(Color.BLACK);
                                produtNameTV.setText(productBO.getProductShortName());
                                produtNameTV.setWidth(mProductNameWidth);
                                mAddViewHorizontalLayout.addView(produtNameTV);
                            }

                            TextView schemeDetailsTV = getNameTv();
                            StringBuffer sb = new StringBuffer();
                         /*   if (buyProductBO.getBuyQty() == buyProductBO.getTobuyQty()) {
                                sb.append((int) buyProductBO.getBuyQty());
                            } else {
                                sb.append((int) buyProductBO.getBuyQty() + " - " + (int) buyProductBO.getTobuyQty());
                            }*/
                            if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                if (buyProductBO.getBuyQty() == buyProductBO.getTobuyQty()) {
                                    sb.append((int) buyProductBO.getBuyQty());
                                } else {
                                    sb.append((int) buyProductBO.getBuyQty() + " - " + (int) buyProductBO.getTobuyQty());
                                }
                                if (buyProductBO.getUomID() == productBO.getCaseUomId()) {
                                    sb.append(" " + buyProductBO.getUomDescription());

                                } else if (buyProductBO.getUomID() == productBO.getOuUomid()) {
                                    sb.append(" " + buyProductBO.getUomDescription());

                                } else {
                                    sb.append(" " + buyProductBO.getUomDescription());
                                }
                            } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                if (buyProductBO.getBuyQty() == buyProductBO.getTobuyQty()) {
                                    sb.append(buyProductBO.getBuyQty());
                                } else {
                                    sb.append(buyProductBO.getBuyQty() + " - " + buyProductBO.getTobuyQty());
                                }
                                sb.append(" " + rupeesLabel);
                            }

                            schemeDetailsTV.setTextColor(getResources().getColor(R.color.FullBlack));
                            schemeDetailsTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                            schemeDetailsTV.setText(sb.toString());
                            schemeDetailsTV.setWidth(mSchemeDetailWidth);
                            mAddViewHorizontalLayout.addView(schemeDetailsTV);

                        }


                    }
                    if (j == 0) {
                        mAddViewLayout.addView(headerLayout);
                    }
                    mAddViewLayout.addView(mAddViewHorizontalLayout);
                    j++;


                }


            }
        }
        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;

    }


    private HorizontalScrollView addViewANYLogicGET(int schemeParentId, String groupName) {
        final int maximumLineCount = bmodel.schemeDetailsMasterHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.bottomMargin = 20;

        mAddViewLayout = new LinearLayout(ctxt);

        layoutParams1.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(ctxt);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                StringBuffer sb = new StringBuffer();


                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);

                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setTextColor(Color.BLACK);
                            productTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            productTV.setText(productBO.getProductShortName());
                            productTV.setWidth(mProductNameWidth);
                            childHeaderView.addView(productTV);
                        }


                    }
                }

            }


            mAddViewLayout.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));

                if (schemeBO != null) {
                    List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                    if (freeProductList != null && freeProductList.size() > 0) {
                        SchemeProductBO freeProductBO = freeProductList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {
                            LinearLayout schemeChildView = new LinearLayout(ctxt);
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            TextView schemeNameTV = getNameTv();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : freeProductList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {
                                    TextView tv = getNameTv();
                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                    tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    if (productBO != null) {

                                        if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        } else {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        }
                                        tv.setWidth(mSchemeDetailWidth);

                                        tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                        tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                                        schemeChildView.addView(tv);
                                        break;
                                    }

                                }


                            }
                            mAddViewLayout.addView(schemeChildView);


                        }
                    }
                }
            }


        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }

    private HorizontalScrollView addViewONLYLogicGET(int schemeParentId, String groupName) {
        final int maximumLineCount = bmodel.schemeDetailsMasterHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.bottomMargin = 20;

        mAddViewLayout = new LinearLayout(ctxt);

        layoutParams1.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(ctxt);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                        productTV.setTextColor(Color.BLACK);
                        productTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setText(productBO.getProductShortName());
                        }
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(productTV);
                    }
                }
            }


            mAddViewLayout.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));

                if (schemeBO != null) {
                    List<SchemeProductBO> freeProductsList = schemeBO.getFreeProducts();
                    if (freeProductsList != null && freeProductsList.size() > 0) {
                        SchemeProductBO freeProductBO = freeProductsList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {
                            LinearLayout schemeChildView = new LinearLayout(ctxt);
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            TextView schemeNameTV = getNameTv();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : freeProductsList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {
                                    TextView tv = getNameTv();
                                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    if (productBO != null) {

                                        if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        } else {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                        }
                                        tv.setWidth(mSchemeDetailWidth);
                                        tv.setSingleLine(true);


                                    }
                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                    tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                                    schemeChildView.addView(tv);
                                }
                            }
                            mAddViewLayout.addView(schemeChildView);
                        }
                    }

                }
            }

        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }


    private HorizontalScrollView addViewSchemeFreeeLogicNew(int schemeparentId) {
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ctxt);
        LinearLayout childView1 = null;
        LinearLayout childView2 = null;
        LinearLayout childView3 = null;
        layoutParams1.setMargins(0, 5, 0, 0);
        layoutParams1.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams1);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mAddViewLayout = new LinearLayout(ctxt);

        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(ctxt.getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(ctxt.getResources().getDrawable(R.drawable.border));
        }


        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeparentId);
        boolean isPercentageDiscAvailable = false;
        boolean isAmountDiscAvailable = false;
        boolean isPriceDiscAvailable = false;
        for (String schemeId : schemeList) {
            final SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeId);
            if (schemeBO != null) {
                final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                if (freeProductList != null && freeProductList.size() > 0) {
                    final SchemeProductBO schemeProductBO = freeProductList.get(0);
                    if (schemeProductBO.getMaxAmount() > 0) {
                        isAmountDiscAvailable = true;
                    }
                    if (schemeProductBO.getPriceMaximum() > 0) {
                        isPriceDiscAvailable = true;
                    }
                    if (schemeProductBO.getMaxPercent() > 0) {
                        isPercentageDiscAvailable = true;
                    }
                }
            }
        }

        final int size = schemeList.size();

        LinearLayout headerView = new LinearLayout(ctxt);
        LinearLayout.LayoutParams layoutParamsMatchparent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(layoutParamsMatchparent);
        headerView.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = getTextViewTitle(false, Gravity.CENTER, false);
        tv.setText("Type");
        tv.setWidth(mSchemeDetailWidth);
        tv.setLayoutParams(layoutParams);


        for (int i = size - 1; i >= 0; i--) {
            SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));
            if (schemeBO != null) {

                TextView schemeNameTV = getTextViewTitle(false, Gravity.CENTER, false);
                schemeNameTV.setText(schemeBO.getScheme());
                schemeNameTV.setLayoutParams(layoutParamsMatchparent);
                schemeNameTV.setWidth(mSchemeDetailWidth);
                if (i == 0) {
                    mAddViewLayout.addView(headerView);
                }

                final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                if (freeProductList != null && freeProductList.size() > 0) {
                    SchemeProductBO freeproductBO = freeProductList.get(0);

                    if (isAmountDiscAvailable) {
                        if (childView1 == null) {
                            childView1 = new LinearLayout(ctxt);
                            childView1.setOrientation(LinearLayout.HORIZONTAL);
                            childView1.setLayoutParams(layoutParams);
                            TextView amountTitleTV = getTextViewTitle(false, Gravity.LEFT, false);
                            amountTitleTV.setText("Amount");
                            amountTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            amountTitleTV.setTextColor(Color.BLACK);
                            amountTitleTV.setWidth(mProductNameWidth);
                            amountTitleTV.setLayoutParams(layoutParams);
                            childView1.addView(amountTitleTV);
                        }

                        TextView amountTV = getTextViewTitle(false, Gravity.CENTER, false);
                        amountTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        amountTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.FullBlack));
                        amountTV.setWidth(mSchemeDetailWidth);

                        if (freeproductBO.getMinAmount() == freeproductBO.getMaxAmount()) {
                            amountTV.setText(bmodel.formatValue(freeproductBO.getMinAmount()));

                        } else {
                            amountTV.setText(bmodel.formatValue(freeproductBO.getMinAmount()) + " - " + bmodel.formatValue(freeproductBO.getMaxAmount()));
                        }

                        amountTV.setLayoutParams(layoutParams);
                        childView1.addView(amountTV);

                        if (i == 0) {
                            mAddViewLayout.addView(childView1);
                        }


                    }
                    if (isPercentageDiscAvailable) {
                        if (childView2 == null) {
                            childView2 = new LinearLayout(ctxt);
                            childView2.setOrientation(LinearLayout.HORIZONTAL);
                            childView2.setLayoutParams(layoutParams);
                            TextView distTitleTV = getTextViewTitle(false, Gravity.LEFT, false);
                            distTitleTV.setTextColor(Color.BLACK);
                            distTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            distTitleTV.setText("%Disc");
                            distTitleTV.setWidth(mProductNameWidth);
                            childView2.addView(distTitleTV);
                        }

                        TextView percentTV = getTextViewTitle(false, Gravity.CENTER, false);
                        percentTV.setTextColor(Color.BLACK);

                        if (freeproductBO.getMinPercent() == freeproductBO.getMaxPercent()) {
                            percentTV.setText(freeproductBO.getMinPercent() + "");
                        } else {
                            percentTV.setText(freeproductBO.getMinPercent() + " - " + freeproductBO.getMaxPercent());
                        }

                        percentTV.setLayoutParams(layoutParams);
                        percentTV.setWidth(mSchemeDetailWidth);
                        childView2.addView(percentTV);
                        if (i == 0) {
                            mAddViewLayout.addView(childView2);
                        }


                    }
                    if (isPriceDiscAvailable) {
                        if (childView3 == null) {
                            childView3 = new LinearLayout(ctxt);
                            childView3.setOrientation(LinearLayout.HORIZONTAL);

                            TextView priceTitleTV = getTextViewTitle(false, Gravity.LEFT, false);
                            priceTitleTV.setTextColor(Color.BLACK);
                            priceTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            priceTitleTV.setText("Price");
                            priceTitleTV.setLayoutParams(layoutParams);
                            priceTitleTV.setWidth(mProductNameWidth);
                            childView3.addView(priceTitleTV);
                        }

                        TextView priceTV = getTextViewTitle(false, Gravity.CENTER, false);
                        priceTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.FullBlack));
                        priceTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        if (freeproductBO.getPriceActual() > 0) {
                            if (freeproductBO.getPriceActual() == freeproductBO.getPriceMaximum()) {
                                priceTV.setText(freeproductBO.getPriceActual() + "");
                            } else {
                                priceTV.setText(freeproductBO.getPriceActual() + " - " + freeproductBO.getPriceMaximum());
                            }
                        } else {
                            priceTV.setText("-");
                        }
                        priceTV.setLayoutParams(layoutParams);
                        priceTV.setWidth(mSchemeDetailWidth);
                        childView3.addView(priceTV);
                        if (i == 0) {
                            mAddViewLayout.addView(childView3);
                        }
                    }


                }

            }
        }
        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }


    private TextView getTextViewTitle(boolean isBackGround, int aligntment, boolean isTextColor) {
        TextView tvMatchparent = new TextView(
                ctxt);
        tvMatchparent.setLayoutParams(linearlprams);
        tvMatchparent.setGravity(aligntment);
        tvMatchparent.setPadding(10, 4, 0, 4);
        tvMatchparent.setBackgroundColor(ctxt.getResources().getColor(R.color.white));

        if (isBackGround)
            tvMatchparent.setBackgroundColor(ctxt.getResources().getColor(R.color.BLUE));
        if (isTextColor)
            tvMatchparent.setTextColor(ctxt.getResources().getColor(R.color.BLUE));


        return tvMatchparent;
    }

    private TextView getNameTv() {
        final TextView verticalSeperator6 = new TextView(
                bmodel.getActivity());

        verticalSeperator6
                .setGravity(Gravity.CENTER_VERTICAL
                        | Gravity.CENTER_HORIZONTAL);
        verticalSeperator6.setTextSize(mTextViewSize);

        verticalSeperator6.setPadding(10, 4, 0, 4);
        verticalSeperator6.setSingleLine(false);


        return verticalSeperator6;
    }


    private TextView getVerticalLine() {
        TextView verticalLineTV = new TextView(ctxt);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalLineTV.setLayoutParams(layoutParams);
        verticalLineTV.setBackgroundColor(ctxt.getResources().getColor(R.color.BLUE));
        return verticalLineTV;
    }

    private TextView getHorizontalLine() {
        TextView horizontalLine = new TextView(ctxt);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((mTotalScreenWidth * 97) / (100), 1);
        horizontalLine.setLayoutParams(layoutParams);
        horizontalLine.setBackgroundColor(ctxt.getResources().getColor(R.color.edit_text_grey));
        horizontalLine.setPadding(0, 20, 0, 20);
        horizontalLine.setGravity(Gravity.CENTER);
        return horizontalLine;
    }

    /**
     * Get the TextView height before the TextView will render
     *
     * @param textView the TextView to measure
     * @return the height of the textView
     */
    public static int getTextViewHeight(TextView textView) {
        WindowManager wm =
                (WindowManager) textView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    private LinearLayout addSlabWiseFreeProductANDLogic(String schemeId, String groupName) {

        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        mAddViewLayout = new LinearLayout(ctxt);

        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }
        LinearLayout childHeaderView = new LinearLayout(ctxt);
        childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        childHeaderView.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = getNameTv();
        txt.setWidth(mSlabWiseProductNameWidth);

        txt.setText("SKU");

        childHeaderView.addView(txt);
        TextView schemeNameTV = getNameTv();
        SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeId);
        schemeNameTV.setText(schemeBO.getScheme());
        schemeNameTV.setWidth(mSlabWiseSchemeNameWidth);
        childHeaderView.addView(schemeNameTV);
        mAddViewLayout.addView(childHeaderView);
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        for (SchemeProductBO freeProductBO : freeProductList) {
            if (groupName.equals(freeProductBO.getGroupName())) {


                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(freeProductBO.getProductId());
                if (productBO != null) {
                    childHeaderView = new LinearLayout(ctxt);
                    childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    childHeaderView.setOrientation(LinearLayout.HORIZONTAL);
                    TextView productNameTV = getNameTv();
                    productNameTV.setText(productBO.getProductShortName());
                    productNameTV.setWidth(mSlabWiseProductNameWidth);
                    childHeaderView.addView(productNameTV);

                    String freeQty = "";
                    if (freeProductBO.getQuantityMinimum() == freeProductBO.getQuantityMaximum()) {
                        freeQty = freeProductBO.getQuantityMinimum() + "";
                    } else {
                        freeQty = freeProductBO.getQuantityMinimum() + "-" + freeProductBO.getQuantityMaximum();
                    }
                    if (freeProductBO.getUomID() == productBO.getCaseUomId()) {
                        freeQty += freeProductBO.getUomDescription();
                    } else if (freeProductBO.getUomID() == productBO.getOuUomid()) {
                        freeQty += freeProductBO.getUomDescription();
                    } else if (freeProductBO.getUomID() == productBO.getPcUomid()) {
                        freeQty += freeProductBO.getUomDescription();
                    }
                    TextView slabValueTV = getNameTv();
                    slabValueTV.setWidth(mSlabWiseSchemeNameWidth);
                    slabValueTV.setText(freeQty);

                    childHeaderView.addView(slabValueTV);
                    mAddViewLayout.addView(childHeaderView);
                }

            }
        }


        return mAddViewLayout;


    }

    private LinearLayout addSlabWiseFreeProductANYLogic(String schemeId, String groupName) {
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        mAddViewLayout = new LinearLayout(ctxt);

        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }
        LinearLayout childHeaderView = new LinearLayout(ctxt);
        childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        childHeaderView.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = getNameTv();

        txt.setText("SKU");
        txt.setWidth(mSlabWiseProductNameWidth);

        childHeaderView.addView(txt);
        TextView schemeNameTV = getNameTv();
        SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeId);
        schemeNameTV.setText(schemeBO.getScheme());
        schemeNameTV.setWidth(mSlabWiseSchemeNameWidth);
        childHeaderView.addView(schemeNameTV);
        mAddViewLayout.addView(childHeaderView);
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        StringBuffer sb = new StringBuffer();
        childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        childHeaderView.setOrientation(LinearLayout.VERTICAL);
        TextView productNameTV = getNameTv();
        int fromQty = 0, toQty = 0;
        String uomDes = "";
        for (SchemeProductBO freeProductBO : freeProductList) {
            if (freeProductBO.getGroupName().equals(groupName)) {


                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(freeProductBO.getProductId());
                if (productBO != null) {
                    sb.append(productBO.getProductShortName());
                    sb.append("\n");


                }
                fromQty = (int) freeProductBO.getBuyQty();
                toQty = (int) freeProductBO.getTobuyQty();
                if (freeProductBO.getUomID() == productBO.getCaseUomId()) {
                    uomDes = freeProductBO.getUomDescription();
                } else if (freeProductBO.getUomID() == productBO.getOuUomid()) {
                    uomDes = freeProductBO.getUomDescription();
                } else if (freeProductBO.getUomID() == productBO.getPcUomid()) {
                    uomDes = freeProductBO.getUomDescription();
                }
            }


        }
        String freeQty = "";
        if (fromQty == toQty) {
            freeQty = fromQty + " " + uomDes;
        } else {
            freeQty = fromQty + "-" + toQty + " " + uomDes;
        }
        childHeaderView = new LinearLayout(ctxt);
        childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        childHeaderView.setOrientation(LinearLayout.HORIZONTAL);

        productNameTV.setText(sb.toString());
        productNameTV.setWidth(mSlabWiseProductNameWidth);
        childHeaderView.addView(productNameTV);
        TextView slabTV = getNameTv();
        slabTV.setText(freeQty);
        slabTV.setWidth(mSlabWiseSchemeNameWidth);
        childHeaderView.addView(slabTV);
        mAddViewLayout.addView(childHeaderView);
        return mAddViewLayout;
    }

    private LinearLayout addSlabWiseFreeProductONLYLogic(String schemeId, String groupName) {
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        mAddViewLayout = new LinearLayout(ctxt);

        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        } else {
            mAddViewLayout.setBackgroundColor(Color.WHITE);
        }
        LinearLayout childHeaderView = new LinearLayout(ctxt);
        childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        childHeaderView.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = getNameTv();

        txt.setText("SKU");
        txt.setWidth(mSlabWiseProductNameWidth);

        childHeaderView.addView(txt);
        TextView schemeNameTV = getNameTv();
        SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeId);
        schemeNameTV.setText(schemeBO.getScheme());
        schemeNameTV.setWidth(mSlabWiseSchemeNameWidth);
        childHeaderView.addView(schemeNameTV);
        mAddViewLayout.addView(childHeaderView);
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        for (SchemeProductBO freeProductBO : freeProductList) {

            if (freeProductBO.getGroupName().equals(groupName)) {
                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(freeProductBO.getProductId());
                if (productBO != null) {
                    childHeaderView = new LinearLayout(ctxt);
                    childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    childHeaderView.setOrientation(LinearLayout.HORIZONTAL);
                    TextView productNameTV = getNameTv();
                    productNameTV.setWidth(mSlabWiseProductNameWidth);
                    childHeaderView.addView(productNameTV);

                    String freeQty = "";
                    if (freeProductBO.getQuantityMinimum() == freeProductBO.getQuantityMaximum()) {
                        freeQty = freeProductBO.getQuantityMinimum() + "";
                    } else {
                        freeQty = freeProductBO.getQuantityMinimum() + "-" + freeProductBO.getQuantityMaximum();
                    }
                    if (freeProductBO.getUomID() == productBO.getCaseUomId()) {
                        freeQty += " " + freeProductBO.getUomDescription();
                    } else if (freeProductBO.getUomID() == productBO.getOuUomid()) {
                        freeQty += " " + freeProductBO.getUomDescription();
                    } else if (freeProductBO.getUomID() == productBO.getPcUomid()) {
                        freeQty += " " + freeProductBO.getUomDescription();
                    }
                    TextView slabValueTV = getNameTv();
                    slabValueTV.setText(freeQty);
                    slabValueTV.setWidth(mSlabWiseSchemeNameWidth);

                    childHeaderView.addView(slabValueTV);
                    mAddViewLayout.addView(childHeaderView);
                }

            }
        }
        return mAddViewLayout;
    }
}
