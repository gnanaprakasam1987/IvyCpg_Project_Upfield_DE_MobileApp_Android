package com.ivy.sd.png.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.TempSchemeBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 11/30/2016,2:29 PM.
 */
public class FragmentSchemeDialog extends Fragment {
    private View rootView;

    private BusinessModel bmodel;

    private LinearLayout mainLayout;
    private LinearLayout.LayoutParams linearlprams, linearlpramsHSL, linearlpramsVSL,
            linearlpramsSub1, linearlpramsSub2, linearlpramsSub3,
            linearlpramsSub4;
    private LinearLayout.LayoutParams lprams;
    private Set<SchemeBO> uniqueSchemes;
    private List<SchemeBO> schemeList = null;
    private SpinnerBO tempBo;
    private Spinner schemeSpinner;
    private ArrayAdapter<SpinnerBO> schemeAdapter;
    private List<TempSchemeBO> selectAllList;
    private LinearLayout linearWidgetSchemeReport;
    //private Context ctxt;
    private TabHost tabHost;
    private TabHost.TabSpec setContent1, setContent2;


    private LinearLayout mAddViewLayout;
    private LinearLayout mAddViewHorizontalLayout;
    private static final String PIECE = "Pcs";
    private static final String CASES = "Cases";
    private static final String OUTER = "Outer";
    private static final String RUPEES = "Rs";
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
    private int flag;
    //private String pdname;
    private ProductMasterBO productObj;
    private List<SchemeBO> schemes;
    private TypedArray typearr;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.scheme, container, false);
        //getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        //ctxt = getActivity().getApplicationContext();
        LinearLayout temp = (LinearLayout) rootView.findViewById(R.id.scheme_info_layout);
        temp.setVisibility(View.GONE);
        rootView.findViewById(R.id.product_layout).setBackgroundColor(Color.WHITE);
        productObj = bmodel.selectedPdt;
        schemes = bmodel.schemeDetailsMasterHelper.getmSchemeList();
        this.mProductID = productObj.getProductID();
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mTotalScreenWidth = outMetrics.widthPixels;
        //this.mTotalScreenWidth = getArguments().getInt("totalScreenSize");
        this.flag = 1;//getArguments().getInt("Flag");
        //this.pdname=productObj.getProductName();

        uniqueSchemes = new LinkedHashSet<SchemeBO>();

        rootView.findViewById(R.id.spinner_tab_widget).setVisibility(View.GONE);
        FrameLayout tabcontent = (FrameLayout) rootView.findViewById(android.R.id.tabcontent);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tabcontent.setLayoutParams(frameParams);
        rootView.findViewById(R.id.main_layout).setBackgroundColor(Color.WHITE);
        TextView schemeTitleTV = (TextView) rootView.findViewById(R.id.scheme_info_title);
        mainLayout = (LinearLayout) rootView.findViewById(R.id.schemeDialogwidget);
        linearlpramsSub3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearWidgetSchemeReport = new LinearLayout(getActivity());
        linearWidgetSchemeReport.setLayoutParams(linearlpramsSub3);
        linearWidgetSchemeReport
                .setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));//Color.parseColor("#9CE7F9"));
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

        tabHost = (TabHost) rootView.findViewById(R.id.TabHost01);
        tabHost.setup();
        tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
        setContent1 = tabHost
                .newTabSpec("tab1")
                .setIndicator(
                        createTabView(
                                tabHost.getContext(),
                                getContext().getResources().getString(
                                        R.string.Scheme_Details)))
                .setContent(new TabHost.TabContentFactory() {

                    @Override
                    public View createTabContent(String tag) {
                        return new TextView(getActivity());
                    }
                });

        setContent2 = tabHost
                .newTabSpec("tab2")
                .setIndicator(
                        createTabView(
                                tabHost.getContext(),
                                getContext().getResources().getString(
                                        R.string.Product_details)))
                .setContent(new TabHost.TabContentFactory() {

                    @Override
                    public View createTabContent(String tag) {
                        return new TextView(getActivity());
                    }
                });

        schemeSpinner = (Spinner) rootView.findViewById(R.id.scheme_spnr);
        if (flag == 1) {

            TextView productTitleTV = (TextView) rootView.findViewById(R.id.product_info_title);

            //productTitleTV.setText(pdname);
            productTitleTV.setVisibility(View.GONE);
            boolean isSchemeAvailable = false;
            if (schemes != null && schemes.size() > 0) {
                for (SchemeBO scbo : schemes) {
                    for (SchemeProductBO isSchemeHavingProd : scbo
                            .getBuyingProducts()) {
                        if (isSchemeHavingProd.getProductId().equals(mProductID))
                            isSchemeAvailable = true;
                    }
                }
            }
            //schemeTitleTV.setText(pdname);
            schemeTitleTV.setVisibility(View.GONE);
            if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                if (schemes != null && schemes.size() > 0 && isSchemeAvailable) {
                    tabHost.addTab(setContent1);
                    schemeTitleTV.setWidth(outMetrics.widthPixels);
                    if (uniqueSchemes != null && !uniqueSchemes.isEmpty())
                        uniqueSchemes.clear();
                    updateSchemeView(mProductID);
                    //setSchemeInfo(pdname, prodId, schemes);
                    tabHost.setCurrentTab(0);
                    rootView.findViewById(R.id.product_tab_widget).setVisibility(
                            View.GONE);
                }
            } else if (bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
                if (schemes != null && schemes.size() > 0 && isSchemeAvailable) {
                    tabHost.addTab(setContent1);
                    schemeTitleTV.setWidth(outMetrics.widthPixels);
                    if (uniqueSchemes != null && !uniqueSchemes.isEmpty())
                        uniqueSchemes.clear();
                    // setSchemeInfo(pdname, prodId, schemes);
                    updateSchemeView(mProductID);
                    tabHost.setCurrentTab(0);
                    rootView.findViewById(R.id.product_tab_widget).setVisibility(
                            View.GONE);
                } else {
                    tabHost.setCurrentTab(1);
                    rootView.findViewById(R.id.scheme_tab_widget).setVisibility(
                            View.GONE);
                    rootView.findViewById(R.id.product_tab_widget).setVisibility(
                            View.VISIBLE);
                }
                productTitleTV.setWidth(outMetrics.widthPixels);
                setProductInfo(productObj);
                tabHost.addTab(setContent2);
            } else {
                productTitleTV.setWidth(outMetrics.widthPixels);
                setProductInfo(productObj);
                tabHost.addTab(setContent2);
                tabHost.setCurrentTab(1);
                rootView.findViewById(R.id.scheme_tab_widget).setVisibility(View.GONE);
                rootView.findViewById(R.id.product_tab_widget).setVisibility(
                        View.VISIBLE);
            }
        } else if (flag == 0) {
            schemeTitleTV.setVisibility(View.GONE);
            /*LinearLayout temp = (LinearLayout)rootView. findViewById(R.id.scheme_info_layout);
            temp.setVisibility(View.GONE);*/
            schemeSpinner.setVisibility(View.VISIBLE);
            LinearLayout lr = (LinearLayout) rootView.findViewById(R.id.spinner_tab_widget);
            android.view.ViewGroup.LayoutParams lp = lr.getLayoutParams();
            lp.width = outMetrics.widthPixels;
            lr.setLayoutParams(lp);

            // schemeTitleTV.setWidth(outMetrics.widthPixels);
            schemeAdapter = new ArrayAdapter<SpinnerBO>(getActivity().getApplicationContext(),
                    android.R.layout.simple_spinner_item);
            schemeAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            schemeAdapter.add(new SpinnerBO(0, getActivity().getResources()
                    .getString(R.string.select)));
            schemeAdapter.add(new SpinnerBO(1, getActivity().getResources()
                    .getString(R.string.all)));
            for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {
                if (bo.isPromo())
                    schemeAdapter.add(new SpinnerBO(Integer.parseInt(bo
                            .getProductID()), bo.getProductName()));
            }
            schemeSpinner.setAdapter(schemeAdapter);
            tabHost.addTab(setContent1);
            tabHost.setCurrentTab(0);
            rootView.findViewById(R.id.product_tab_widget).setVisibility(View.GONE);
        }

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("tab1")) {
                    rootView.findViewById(R.id.scheme_tab_widget).setVisibility(
                            View.VISIBLE);
                    rootView.findViewById(R.id.product_tab_widget).setVisibility(
                            View.GONE);
                } else if (tabId.equals("tab2")) {
                    rootView.findViewById(R.id.scheme_tab_widget).setVisibility(
                            View.GONE);
                    rootView.findViewById(R.id.product_tab_widget).setVisibility(
                            View.VISIBLE);
                }
            }
        });

        return rootView;
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private void setProductInfo(ProductMasterBO productMasterBO) {
        try {
            Vector<ConfigureBO> config = bmodel.configurationMasterHelper
                    .getProductDetails();

            Vector<ConfigureBO> priceDetails = new Vector<>();
            Vector<ConfigureBO> sizeDetails = new Vector<>();
            Vector<ConfigureBO> commonDetails = new Vector<>();
            Vector<ConfigureBO> prodDetails = new Vector<>();
            Commons.print("config size" + config.size());
            for (ConfigureBO con : config) {
                if (con.getConfigCode().equalsIgnoreCase("PRODET03")
                        || con.getConfigCode().equalsIgnoreCase("PRODET10")
                        || con.getConfigCode().equalsIgnoreCase("PRODET11")
                        || con.getConfigCode().equalsIgnoreCase("PRODET12")) {
                    priceDetails.add(con);

                } else if (con.getConfigCode().equalsIgnoreCase("PRODET01")
                        || con.getConfigCode().equalsIgnoreCase("PRODET02")) {
                    sizeDetails.add(con);
                } else if (con.getConfigCode().equalsIgnoreCase("PRODET04")
                        || con.getConfigCode().equalsIgnoreCase("PRODET05")
                        || con.getConfigCode().equalsIgnoreCase("PRODET07")
                        || con.getConfigCode().equalsIgnoreCase("PRODET08")
                        || con.getConfigCode().equalsIgnoreCase("PRODET09")) {
                    commonDetails.add(con);
                } else if (con.getConfigCode().equalsIgnoreCase("PRODET06")
                        || con.getConfigCode().equalsIgnoreCase("PRODET13")) {
                    prodDetails.add(con);
                }

            }
            createDescripView(prodDetails, productMasterBO);
            createView(priceDetails, productMasterBO, 1);
            createView(sizeDetails, productMasterBO, 100);
            createView(commonDetails, productMasterBO, 200);
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private void createDescripView(Vector<ConfigureBO> datails, ProductMasterBO productMasterBO) {
        try {
            LinearLayout ln_base = (LinearLayout) rootView.findViewById(R.id.profile_layout);

            LinearLayout.LayoutParams keyParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 7.0f);
            LinearLayout.LayoutParams valueParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 3.0f);

            LinearLayout ln_card_base = new LinearLayout(getActivity());
            ln_card_base.setOrientation(LinearLayout.VERTICAL);

            //CardView cardView = new CardView(getActivity());


            for (ConfigureBO con : datails) {

                TextView keyTv = new TextView(getActivity());
                keyTv.setPadding(2, 2, 0, 2);
                keyTv.setText(con.getMenuName());

                TextView valueTv = new TextView(getActivity());
                valueTv.setPadding(2, 2, 0, 2);
                valueTv.setTextColor(Color.BLACK);

                LinearLayout ln = new LinearLayout(getActivity());

                if (con.getConfigCode().equalsIgnoreCase("PRODET06")) {
                    valueTv.setText(": " + productMasterBO.getSIH());
                    ln.addView(keyTv, keyParam);
                    ln.addView(valueTv, valueParam);
                    ln_base.addView(ln);
                } else if (con.getConfigCode().equalsIgnoreCase("PRODET13")) {
                    valueTv.setText(": " + productMasterBO.getDescription());
                    ln.addView(keyTv, keyParam);
                    ln.addView(valueTv, valueParam);
                    ln_base.addView(ln, 0);
                }
            }
            /*cardView.addView(ln_card_base);
            ln_base.addView(cardView);*/
            //ln_base.addView(ln_card_base);
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private void createView(Vector<ConfigureBO> datails, ProductMasterBO productMasterBO, int startingId) {
        try {
            LinearLayout ln_base = (LinearLayout) rootView.findViewById(R.id.profile_layout);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            int position = startingId;

            LinearLayout ln_card_base = new LinearLayout(getActivity());
            ln_card_base.setOrientation(LinearLayout.VERTICAL);

            //CardView cardView = new CardView(getActivity());

            for (ConfigureBO con : datails) {


                TextView keyTv = new TextView(getActivity());
                keyTv.setPadding(2, 2, 0, 2);
                keyTv.setText(con.getMenuName());

                TextView valueTv = new TextView(getActivity());
                valueTv.setPadding(2, 2, 0, 2);
                valueTv.setTextColor(Color.BLACK);

                if (con.getConfigCode().equalsIgnoreCase("PRODET03"))
                    valueTv.setText(": " + bmodel.formatValue(productMasterBO.getSrp()));
                else if (con.getConfigCode().equalsIgnoreCase("PRODET10"))
                    valueTv.setText(": " + productMasterBO.getCsrp());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET11"))
                    valueTv.setText(": " + productMasterBO.getOsrp());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET12"))
                    valueTv.setText(": " + productMasterBO.getBaseprice());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET01"))
                    valueTv.setText(": " + productMasterBO.getCaseSize());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET02"))
                    valueTv.setText(": " + productMasterBO.getOutersize());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET04"))
                    valueTv.setText(": " + productMasterBO.getRetailerWiseProductWiseP4Qty());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET05"))
                    valueTv.setText(": " + productMasterBO.getRetailerWiseP4StockQty());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET07"))
                    valueTv.setText(": " + productMasterBO.getWSIH());
                else if (con.getConfigCode().equalsIgnoreCase("PRODET08"))
                    valueTv.setText(": " + productMasterBO.getBarCode());

                if (rootView.findViewById(position) != null) {
                    LinearLayout ln = (LinearLayout) rootView.findViewById(position);
                    if (ln.getChildCount() <= 2) {
                        ln.addView(keyTv, param);
                        ln.addView(valueTv, param);
                    } else {
                        position += 1;
                        LinearLayout ln_new = new LinearLayout(getActivity());
                        ln_new.setId(position);
                        ln_new.addView(keyTv, param);
                        ln_new.addView(valueTv, param);

                        ln_base.addView(ln_new);

                    }
                } else {
                    LinearLayout ln = new LinearLayout(getActivity());
                    ln.setId(position);
                    ln.addView(keyTv, param);
                    ln.addView(valueTv, param);

                    ln_base.addView(ln);

                    /*cardView.addView(ln_card_base);
                    ln_base.addView(cardView);*/
                    //ln_base.addView(ln_card_base);
                }

            }

        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private void updateSchemeView(String productid) {

        ArrayList<Integer> parentIdList = bmodel.schemeDetailsMasterHelper.getParentIdListByProductId().get(productid);
        if (parentIdList != null) {
            for (Integer parentId : parentIdList) {
                final ArrayList<String> schemeIdList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(parentId);
                if (schemeIdList.size() == 1) {
                    mSchemeDetailWidth = (mTotalScreenWidth) / 4 - 20;
                    mProductNameWidth = mTotalScreenWidth - mSchemeDetailWidth - 40;


                } else {
                    mProductNameWidth = 350;
                    mSchemeDetailWidth = 100;
                }

                SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeIdList.get(0));
                if (schemeBO != null) {

                    TextView schemeTitleTV = getTextViewTitle(false, Gravity.LEFT, true);
                    schemeTitleTV.setText(schemeBO.getProductName());
                    schemeTitleTV.setTextColor(Color.GREEN);
                    schemeTitleTV.setTextSize(20);
                    mainLayout.addView(schemeTitleTV);
                    final List<SchemeProductBO> schemeBuyList = schemeBO.getBuyingProducts();
                    List<SchemeProductBO> schemeFreeList = schemeBO.getFreeProducts();

                    ArrayList<String> groupName = new ArrayList<>();
                    if (schemeBuyList != null && schemeBuyList.size() > 0) {
                        TextView buyTitleTV = getTextViewTitle(true, Gravity.CENTER, false);
                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        layoutParams1.setMargins(0, 10, 0, 10);
                        buyTitleTV.setLayoutParams(layoutParams1);
                        buyTitleTV.setText("BUY");
                        buyTitleTV.setWidth(150);
                        buyTitleTV.setTextColor(Color.WHITE);
                        mainLayout.addView(buyTitleTV);
                        int i = 0;
                        for (SchemeProductBO buyProductBO : schemeBuyList) {
                            if (!groupName.contains(buyProductBO.getGroupName())) {
                                i = i + 1;
                                if (i > 1) {
                                    TextView groupLogicType = getTextViewTitle(false, Gravity.CENTER, false);

                                    groupLogicType.setText(schemeBO.getType());
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


                    for (String schemeId : schemeIdList) {

                        final SchemeBO schemeBO1 = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeId);

                        schemeFreeList = schemeBO1.getFreeProducts();


                        if (schemeFreeList != null && schemeFreeList.size() > 0 && !isAlreadyAddedFreeProduct) {

                            int i = 0;
                            for (SchemeProductBO freeProductBO : schemeFreeList) {
                                if (freeProductBO.getGroupName() != null) {
                                    if (!groupName.contains(freeProductBO.getGroupName())) {
                                        if (i == 0) {
                                            isAlreadyAddedFreeProduct = true;
                                            TextView freeTitleTV = getTextViewTitle(true, Gravity.CENTER, true);
                                            freeTitleTV.setText("GET FreeProduct");
                                            freeTitleTV.setWidth(150);

                                            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                            layoutParams1.setMargins(0, 10, 0, 10);
                                            freeTitleTV.setLayoutParams(layoutParams1);
                                            freeTitleTV.setTextColor(Color.WHITE);
                                            mainLayout.addView(freeTitleTV);
                                        }
                                        i++;
                                        if (i > 1) {
                                            TextView groupLogicType = getTextViewTitle(false, Gravity.CENTER, false);

                                            groupLogicType.setText(schemeBO.getFreeType());
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
                                    groupName.add(freeProductBO.getGroupName());
                                }

                            }
                        }
                        if (schemeFreeList != null && schemeFreeList.size() > 0 && !isAlreadyAddedOtherDisc) {
                            SchemeProductBO freeProductBO = schemeFreeList.get(0);
                            if (freeProductBO != null && (freeProductBO.getMaxPercent() > 0 || freeProductBO.getMaxAmount() > 0 || freeProductBO.getPriceMaximum() > 0)) {
                                TextView freeTitleTV = getTextViewTitle(true, Gravity.CENTER, true);
                                freeTitleTV.setText("GET Discount");
                                freeTitleTV.setWidth(150);
                                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                layoutParams1.setMargins(0, 10, 0, 10);
                                freeTitleTV.setLayoutParams(layoutParams1);
                                freeTitleTV.setTextColor(Color.WHITE);

                                mainLayout.addView(freeTitleTV);

                                isAlreadyAddedOtherDisc = true;

                                final LinearLayout ll = addViewSchemeFreeeLogicNew(parentId);
                                mainLayout.addView(ll);


                            }
                        }
                    }


                }


            }

        }

    }


    private LinearLayout addViewSchemeFreeeLogicNew(int schemeparentId) {


        LinearLayout childView1 = null;
        LinearLayout childView2 = null;
        LinearLayout childView3 = null;
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        mAddViewLayout = new LinearLayout(getActivity());

        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
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

        LinearLayout headerView = new LinearLayout(getActivity());

        headerView.setLayoutParams(layoutParams);
        headerView.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = getTextViewTitle(false, Gravity.CENTER, false);
        tv.setText("Type");
        tv.setLayoutParams(layoutParams);
        headerView.addView(tv);


        for (int i = size - 1; i >= 0; i--) {
            SchemeBO schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(i));
            if (schemeBO != null) {

                TextView schemeNameTV = getTextViewTitle(false, Gravity.CENTER, false);
                schemeNameTV.setText(schemeBO.getScheme());
                schemeNameTV.setLayoutParams(layoutParams);
                headerView.addView(getVerticalLine());
                headerView.addView(schemeNameTV);
                if (i == 0) {
                    headerView.addView(getVerticalLine());
                    mAddViewLayout.addView(headerView);
                }

                final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                if (freeProductList != null && freeProductList.size() > 0) {
                    SchemeProductBO freeproductBO = freeProductList.get(0);

                    if (isAmountDiscAvailable) {
                        if (childView1 == null) {
                            childView1 = new LinearLayout(getActivity());
                            childView1.setOrientation(LinearLayout.HORIZONTAL);
                            childView1.setLayoutParams(layoutParams);
                            TextView amountTitleTV = getTextViewTitle(false, Gravity.CENTER, false);
                            amountTitleTV.setText("Amount");
                            amountTitleTV.setLayoutParams(layoutParams);
                            childView1.addView(amountTitleTV);
                        }

                        TextView amountTV = getTextViewTitle(false, Gravity.CENTER, false);
                        if (freeproductBO.getMinAmount() > 0) {
                            if (freeproductBO.getMinAmount() == freeproductBO.getMaxAmount()) {
                                amountTV.setText(freeproductBO.getMinAmount() + "");
                            } else {
                                amountTV.setText(freeproductBO.getMinAmount() + " - " + freeproductBO.getMaxAmount());
                            }
                        } else {
                            amountTV.setText("-");
                        }
                        amountTV.setLayoutParams(layoutParams);
                        childView1.addView(getVerticalLine());
                        childView1.addView(amountTV);

                        if (i == 0) {
                            mAddViewLayout.addView(getHorizontalLine());
                            mAddViewLayout.addView(childView1);
                        }


                    }
                    if (isPercentageDiscAvailable) {
                        if (childView2 == null) {
                            childView2 = new LinearLayout(getActivity());
                            childView2.setOrientation(LinearLayout.HORIZONTAL);
                            childView2.setLayoutParams(layoutParams);
                            TextView distTitleTV = getTextViewTitle(false, Gravity.CENTER, false);
                            distTitleTV.setText("%Disc");
                            distTitleTV.setLayoutParams(layoutParams);
                            childView2.addView(distTitleTV);
                        }

                        TextView percentTV = getTextViewTitle(false, Gravity.CENTER, false);
                        if (freeproductBO.getMinPercent() > 0) {
                            if (freeproductBO.getMinPercent() == freeproductBO.getMaxPercent()) {
                                percentTV.setText(freeproductBO.getMinPercent() + "");
                            } else {
                                percentTV.setText(freeproductBO.getMinPercent() + " - " + freeproductBO.getMaxPercent());
                            }
                        } else {
                            percentTV.setText("-");
                        }
                        percentTV.setLayoutParams(layoutParams);
                        childView2.addView(getVerticalLine());
                        childView2.addView(percentTV);
                        if (i == 0) {
                            mAddViewLayout.addView(getHorizontalLine());
                            mAddViewLayout.addView(childView2);
                        }


                    }
                    if (isPriceDiscAvailable) {
                        if (childView3 == null) {
                            childView3 = new LinearLayout(getActivity());
                            childView3.setOrientation(LinearLayout.HORIZONTAL);

                            TextView priceTitleTV = getTextViewTitle(false, Gravity.CENTER, false);
                            priceTitleTV.setText("Price");
                            priceTitleTV.setLayoutParams(layoutParams);
                            childView3.addView(priceTitleTV);
                        }

                        TextView priceTV = getTextViewTitle(false, Gravity.CENTER, false);
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
                        childView3.addView(getVerticalLine());
                        childView3.addView(priceTV);
                        if (i == 0) {
                            mAddViewLayout.addView(getHorizontalLine());
                            mAddViewLayout.addView(childView3);
                        }
                    }


                }

            }
        }
        return mAddViewLayout;
    }


    private HorizontalScrollView addViewANYLogicBUY(int schemeParentId, String groupName) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        mAddViewLayout = new LinearLayout(getActivity());

        layoutParams1.gravity = Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();

        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(getActivity());
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            if (bmodel.configurationMasterHelper.IS_SCHEME_SLAB_ON)
                txt.setText("SLAB");
            else
                txt.setText("SKU");

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getBuyingProducts();


                childHeaderView.addView(getHorizontalLine());
                if (bmodel.configurationMasterHelper.IS_SCHEME_SLAB_ON) {
                    TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                    productTV.setText(R.string.qty);
                    productTV.setWidth(mProductNameWidth);
                    childHeaderView.addView(productTV);
                } else {
                    for (SchemeProductBO schemeProductBO : buyProdList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            if (productBO != null) {
                                TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
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
                    schemeNameTV.setSingleLine(true);
                    LinearLayout schemeChildView = new LinearLayout(getActivity());
                    schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    schemeChildView.setOrientation(LinearLayout.VERTICAL);
                    schemeChildView.addView(schemeNameTV);
                    List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();
                    for (SchemeProductBO schemeProductBO : buyProductList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {
                            TextView tv = getNameTv();
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            if (productBO != null) {
                                if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                    if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + OUTER);
                                    } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + CASES);
                                    } else {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + PIECE);
                                    }
                                } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                    tv.setText(schemeProductBO.getBuyQty() + "-" + schemeProductBO.getTobuyQty() + " " + RUPEES);
                                }
                                tv.setWidth(mSchemeDetailWidth);


                            }
                            schemeChildView.addView(getHorizontalLine());
                            schemeChildView.addView(tv);
                            break;
                        }


                    }
                    mAddViewLayout.addView(getVerticalLine());
                    mAddViewLayout.addView(schemeChildView);


                }
            }


        }
        horizontalScrollView.addView(mAddViewLayout);


        return horizontalScrollView;
    }

    private HorizontalScrollView addViewONLYLogicBuy(int schemeParentId, String groupName) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        mAddViewLayout = new LinearLayout(getActivity());

        layoutParams1.gravity = Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(getActivity());
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getBuyingProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setText(productBO.getProductShortName());
                        }
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(getHorizontalLine());
                        childHeaderView.addView(productTV);
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
                    schemeNameTV.setSingleLine(true);
                    LinearLayout schemeChildView = new LinearLayout(getActivity());
                    schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    schemeChildView.setOrientation(LinearLayout.VERTICAL);
                    schemeChildView.addView(schemeNameTV);
                    List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();
                    for (SchemeProductBO schemeProductBO : buyProductList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {
                            TextView tv = getNameTv();
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            if (productBO != null) {
                                if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                    if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + OUTER);
                                    } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + CASES);
                                    } else {
                                        tv.setText((int) schemeProductBO.getBuyQty() + "-" + (int) schemeProductBO.getTobuyQty() + " " + PIECE);
                                    }
                                } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                    tv.setText(schemeProductBO.getBuyQty() + "-" + schemeProductBO.getTobuyQty() + " " + RUPEES);
                                }
                                tv.setWidth(mSchemeDetailWidth);
                                tv.setSingleLine(true);


                            }
                            schemeChildView.addView(getHorizontalLine());
                            schemeChildView.addView(tv);
                        }
                    }
                    mAddViewLayout.addView(getVerticalLine());
                    mAddViewLayout.addView(schemeChildView);


                }
            }


        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }

    private TextView getTextViewTitle(boolean isBackGround, int aligntment, boolean isTextColor) {
        TextView tvMatchparent = new TextView(
                getActivity());
        tvMatchparent.setLayoutParams(linearlprams);
        tvMatchparent.setGravity(aligntment);
        tvMatchparent.setPadding(4, 4, 0, 4);

        if (isBackGround)
            tvMatchparent.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));//getActivity().getResources().getColor(R.color.BLUE));//typearr.getColor(R.styleable.MyTextView_primarycolor, 0)
        if (isTextColor)
            tvMatchparent.setTextColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));


        return tvMatchparent;
    }

    private HorizontalScrollView addViewAndLogicBuyNew(int parentId, String groupName) {

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mAddViewLayout = new LinearLayout(getActivity());

        layoutParams1.gravity = Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
        }
        final ArrayList<String> productIdList = bmodel.schemeDetailsMasterHelper.getProductIdListByParentId().get(parentId);
        if (productIdList != null) {

            final ArrayList<String> schemeIdList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(parentId);
            int size = schemeIdList.size();
            int j = 0;

            for (String productId : productIdList) {
                LinearLayout headerLayout = new LinearLayout(getActivity());
                headerLayout.setOrientation(LinearLayout.HORIZONTAL);
                headerLayout.setLayoutParams(layoutParams1);
                mAddViewHorizontalLayout = new LinearLayout(getActivity());
                mAddViewHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                mAddViewHorizontalLayout.setLayoutParams(layoutParams1);
                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(productId);
                if (productBO != null) {
                    if (j == 0) {
                        TextView headerTV = getTextViewTitle(false, Gravity.CENTER, false);
                        headerTV.setText("SKU");
                        headerTV.setWidth(mProductNameWidth);
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
                                headerLayout.addView(getVerticalLine());
                                headerLayout.addView(schemeNameTV);

                            }
                        }


                        final SchemeProductBO buyProductBO = bmodel.schemeDetailsMasterHelper.getBuyProductBOBySchemeidWithPid().get(schemeid + productId);
                        if (buyProductBO != null && buyProductBO.getGroupName().equals(groupName)) {
                            if (i == size - 1) {
                                TextView produtNameTV = getTextViewTitle(false, Gravity.LEFT, false);
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
                                    sb.append(" " + CASES);

                                } else if (buyProductBO.getUomID() == productBO.getOuUomid()) {
                                    sb.append(" " + OUTER);

                                } else {
                                    sb.append(" " + PIECE);
                                }
                            } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                if (buyProductBO.getBuyQty() == buyProductBO.getTobuyQty()) {
                                    sb.append(buyProductBO.getBuyQty());
                                } else {
                                    sb.append(buyProductBO.getBuyQty() + " - " + buyProductBO.getTobuyQty());
                                }
                                sb.append(" " + RUPEES);
                            }

                            schemeDetailsTV.setText(sb.toString());
                            schemeDetailsTV.setWidth(mSchemeDetailWidth);
                            mAddViewHorizontalLayout.addView(getVerticalLine());
                            mAddViewHorizontalLayout.addView(schemeDetailsTV);

                        }


                    }
                    if (j == 0) {
                        mAddViewLayout.addView(headerLayout);
                    }
                    mAddViewLayout.addView(getHorizontalLine());
                    mAddViewLayout.addView(mAddViewHorizontalLayout);
                    j++;


                }


            }
        }
        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;

    }

    private TextView getNameTv() {
        TextView verticalSeperator6 = new TextView(
                getActivity());

        verticalSeperator6
                .setGravity(Gravity.CENTER_VERTICAL
                        | Gravity.CENTER_HORIZONTAL);

        verticalSeperator6.setPadding(0, 4, 0, 4);
        verticalSeperator6.setSingleLine(false);


        return verticalSeperator6;
    }


    private TextView getVerticalLine() {
        TextView verticalLineTV = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalLineTV.setLayoutParams(layoutParams);
        verticalLineTV.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
        return verticalLineTV;
    }

    private TextView getHorizontalLine() {
        TextView horizontalLine = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        horizontalLine.setLayoutParams(layoutParams);
        horizontalLine.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
        return horizontalLine;
    }

    private HorizontalScrollView addViewANDLogicGET(int schemeParentId, String groupName) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAddViewLayout = new LinearLayout(getActivity());

        layoutParams1.gravity = Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(getActivity());
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setText(productBO.getProductShortName());
                        }
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(getHorizontalLine());
                        childHeaderView.addView(productTV);
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
                            LinearLayout schemeChildView = new LinearLayout(getActivity());
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            TextView schemeNameTV = getNameTv();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setSingleLine(true);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : buyProductList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {
                                    TextView tv = getNameTv();
                                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    if (productBO != null) {

                                        if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + OUTER);
                                        } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + CASES);
                                        } else {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + PIECE);
                                        }
                                        tv.setWidth(mSchemeDetailWidth);
                                        tv.setSingleLine(true);


                                    }
                                    schemeChildView.addView(getHorizontalLine());
                                    schemeChildView.addView(tv);
                                }
                            }
                            mAddViewLayout.addView(getVerticalLine());
                            mAddViewLayout.addView(schemeChildView);


                        }
                    }
                }
            }


        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }


    private HorizontalScrollView addViewANYLogicGET(int schemeParentId, String groupName) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        mAddViewLayout = new LinearLayout(getActivity());

        layoutParams1.gravity = Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(getActivity());
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");

            childHeaderView.addView(txt);
            childHeaderView.addView(getHorizontalLine());

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                StringBuffer sb = new StringBuffer();


                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);

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
                    List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                    if (freeProductList != null && freeProductList.size() > 0) {
                        SchemeProductBO freeProductBO = freeProductList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {
                            LinearLayout schemeChildView = new LinearLayout(getActivity());
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            TextView schemeNameTV = getNameTv();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setSingleLine(true);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : freeProductList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {
                                    TextView tv = getNameTv();
                                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    if (productBO != null) {

                                        if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + CASES);
                                        } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + OUTER);
                                        } else {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + PIECE);
                                        }
                                        tv.setWidth(mSchemeDetailWidth);


                                    }
                                    schemeChildView.addView(getHorizontalLine());
                                    schemeChildView.addView(tv);
                                    break;
                                }


                            }
                            mAddViewLayout.addView(getVerticalLine());
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
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        mAddViewLayout = new LinearLayout(getActivity());

        layoutParams1.gravity = Gravity.CENTER;
        horizontalScrollView.setLayoutParams(layoutParams1);


        mAddViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mAddViewLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.border));
        } else {
            mAddViewLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.border));
        }

        ArrayList<String> schemeList = bmodel.schemeDetailsMasterHelper.getSchemeIdlistByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (schemeList != null && size > 0) {
            SchemeBO schemeBO = null;
            LinearLayout childHeaderView = new LinearLayout(getActivity());
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getNameTv();

            txt.setText("SKU");

            childHeaderView.addView(txt);

            schemeBO = bmodel.schemeDetailsMasterHelper.getmSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {
                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {
                        TextView productTV = getTextViewTitle(false, Gravity.LEFT, false);
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            productTV.setText(productBO.getProductShortName());
                        }
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(getHorizontalLine());
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
                            LinearLayout schemeChildView = new LinearLayout(getActivity());
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            TextView schemeNameTV = getNameTv();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setSingleLine(true);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : freeProductsList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {
                                    TextView tv = getNameTv();
                                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    if (productBO != null) {

                                        if (productBO.getCaseUomId() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + OUTER);
                                        } else if (productBO.getOuUomid() == schemeProductBO.getUomID()) {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + CASES);
                                        } else {
                                            tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + PIECE);
                                        }
                                        tv.setWidth(mSchemeDetailWidth);
                                        tv.setSingleLine(true);


                                    }
                                    schemeChildView.addView(getHorizontalLine());
                                    schemeChildView.addView(tv);
                                }
                            }
                            mAddViewLayout.addView(getVerticalLine());
                            mAddViewLayout.addView(schemeChildView);
                        }

                    }


                }
            }


        }

        horizontalScrollView.addView(mAddViewLayout);
        return horizontalScrollView;
    }

}
