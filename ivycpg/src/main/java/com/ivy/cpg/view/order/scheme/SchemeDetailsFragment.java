package com.ivy.cpg.view.order.scheme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;


/**
 * This fragment will show the list of schemes available for the given product
 */
public class SchemeDetailsFragment extends IvyBaseFragment {

    private BusinessModel bModel;
    private Context mContext;
    private SchemeDetailsMasterHelper schemeHelper;

    private String rupeesLabel = "Rs";
    private static final String CURRENCY_LABEL = "currency";
    private static final String BUY_PRODUCT_TITLE_LABEL = "scheme_buy";
    private static final String FREE_PRODUCT_TITLE_LABEL = "scheme_get";
    private static final String SCHEME_BUY_TYPE_QTY = "QTY";
    private static final String SCHEME_BUY_TYPE_VALUE = "SV";
    private static final String AND_LOGIC = "AND";
    private static final String ANY_LOGIC = "ANY";
    private static final String ONLY_LOGIC = "ONLY";
    private String mProductID = "0";
    private String mSelectedSlabId = "0";

    private int mProductNameWidth = 0;
    private int mSchemeDetailWidth = 0;
    private int mTotalScreenWidth = 0;
    private int mTextViewHeight = 0;
    private int mTextViewSize = 14;
    private int mSlabWiseProductNameWidth = 0;
    private int mSlabWiseSchemeNameWidth = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());
        mContext = getActivity().getApplicationContext();
        schemeHelper = SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext());


        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;

        if (mTotalScreenWidth > 1000) {
            mTextViewSize = mTextViewSize - 2;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scheme_details, container, false);

        try {
            setHasOptionsMenu(true);
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setDisplayShowTitleEnabled(false);
            }

            DisplayMetrics outMetrics = new DisplayMetrics();
            getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);


            if (getArguments() != null && getArguments().getString("productId") != null)
                mProductID = getArguments().getString("productId");
            if (getArguments() != null && getArguments().getString("slabId") != null)
                mSelectedSlabId = getArguments().getString("slabId");

            ProductMasterBO productMasterBO = bModel.productHelper.getProductMasterBOById(mProductID);

            if (!mSelectedSlabId.equals("0")) {
                setScreenTitle(getResources().getString(R.string.scheme_details));
                rootView.findViewById(R.id.layout_title).setVisibility(View.GONE);
                prepareView(rootView, mProductID, productMasterBO, mSelectedSlabId);
            } else if (productMasterBO != null) {
                setScreenTitle(productMasterBO.getProductShortName());
                List<SchemeBO> schemes = schemeHelper.getSchemeList();


                if (bModel.labelsMasterHelper.applyLabels(CURRENCY_LABEL) != null) {
                    rupeesLabel = bModel.labelsMasterHelper.applyLabels(CURRENCY_LABEL);
                }

                TextView schemeTitleTV = rootView.findViewById(R.id.scheme_info_title);
                schemeTitleTV.setText(productMasterBO.getProductShortName());


                boolean isSchemeAvailable = false;
                if (schemes != null && schemes.size() > 0) {
                    for (SchemeBO schemeBO : schemes) {
                        for (SchemeProductBO buyProductBO : schemeBO.getBuyingProducts()) {
                            if (buyProductBO.getProductId().equals(productMasterBO.getProductID())||productMasterBO.getParentHierarchy().contains("/" + buyProductBO.getProductId() + "/"))
                                isSchemeAvailable = true;
                        }
                    }
                }

                if (schemes != null && schemes.size() > 0 && isSchemeAvailable) {
                    schemeTitleTV.setWidth(outMetrics.widthPixels);
                    prepareView(rootView, mProductID, productMasterBO, "0");
                }
            } else if ("0".equals(mProductID)) {
                rootView.findViewById(R.id.layout_title).setVisibility(View.GONE);
                prepareView(rootView, mProductID, null, mSelectedSlabId);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }


        return rootView;
    }

    /**
     * used to show pop pup window for scheme description
     *
     * @param x        - x coordinate view position value
     * @param y        - y coordinate view position vlaue
     * @param slabText - return selected text value
     */
    private void showPopupWindow(int x, int y, String slabText) {
        View layout = getActivity().getLayoutInflater().inflate(R.layout.popup_layout, null, false);
        TextView slabTextView = (TextView) layout.findViewById(R.id.slab_desc_txt);

        slabTextView.setText(slabText);

        final PopupWindow popup = new PopupWindow(getActivity());
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth((int) getResources().getDimension(R.dimen.ivy_logo_width));
        popup.setContentView(layout);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(mainLayout, Gravity.LEFT | Gravity.TOP, (x - (popup.getWidth() / 2)), y);
    }

    /**
     * Preparing view to list schemes
     *
     * @param rootView Root view
     * @param mProductId product Id
     * @param mSelectedSlabId If this value not zero then only scheme with this Id will be listed.
     */
    private LinearLayout mainLayout;

    private void prepareView(View rootView, String mProductId, ProductMasterBO productMasterBO, String mSelectedSlabId) {

        mainLayout = rootView.findViewById(R.id.schemeDialogwidget);

        if (schemeHelper.getParentIdListByProductId() == null)
            return;

        ArrayList<Integer> parentIdList = null;
        if (schemeHelper.getParentIdListByProductId().get(mProductId) != null) {
            parentIdList = schemeHelper.getParentIdListByProductId().get(mProductId);
        } else if (productMasterBO != null){
            for (String productId : schemeHelper.getParentIdListByProductId().keySet()) {
                if (productId.equals(productMasterBO.getProductID())|| productMasterBO.getParentHierarchy().contains("/" + productId + "/")) {
                    parentIdList = schemeHelper.getParentIdListByProductId().get(productId);
                }

            }
        }
        if ("0".equals(mProductId))
            parentIdList = schemeHelper.getmParentIDList();

        if (parentIdList != null) {
            for (Integer parentId : parentIdList) {

                if (mSelectedSlabId.equals("0") || String.valueOf(parentId).equals(mSelectedSlabId)) {

                    final ArrayList<String> schemeIdList = schemeHelper.getSchemeIdListByParentID().get(parentId);

                    int prodWidth = ((mTotalScreenWidth) * 40) / 100;
                    int slabWidth = ((mTotalScreenWidth) * 30) / 100;

                    mProductNameWidth = prodWidth;
                    mSchemeDetailWidth = slabWidth;

                    mSlabWiseSchemeNameWidth = (mTotalScreenWidth) / 4 - 20;
                    mSlabWiseProductNameWidth = mTotalScreenWidth - mSlabWiseSchemeNameWidth - 40;


                    SchemeBO schemeBO = null;

                    if (schemeIdList != null)
                        schemeBO = schemeHelper.getSchemeById().get(schemeIdList.get(0));


                    LinearLayout.LayoutParams layoutParamsSchemeTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParamsSchemeTitle.setMargins(0, 20, 0, 10);
                    layoutParamsSchemeTitle.gravity = Gravity.LEFT | Gravity.CENTER;

                    TextView textSchemeTitle = getTextView(false, Gravity.LEFT, true);
                    textSchemeTitle.setText(schemeBO.getProductName());
                    textSchemeTitle.setTextColor(getResources().getColor(R.color.FullBlack));
                    textSchemeTitle.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    textSchemeTitle.setTextSize(mTextViewSize);
                    textSchemeTitle.setGravity(Gravity.START | Gravity.BOTTOM);
                    textSchemeTitle.setLayoutParams(layoutParamsSchemeTitle);

                    mainLayout.addView(textSchemeTitle);


                    final List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();


                    ArrayList<String> groupName = new ArrayList<>();
                    if (schemeBuyProducts != null && schemeBuyProducts.size() > 0) {

                        LinearLayout.LayoutParams layoutParam_BuyProductParent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                        LinearLayout layout_BuyProductParent = new LinearLayout(mContext);
                        layout_BuyProductParent.setLayoutParams(layoutParam_BuyProductParent);
                        layout_BuyProductParent.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));

                        TextView text_BuyProductsTitle = getTextView(true, Gravity.LEFT, false);
                        LinearLayout.LayoutParams layoutParams_BuyProductTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams_BuyProductTitle.setMargins(0, 10, 0, 10);
                        layoutParams_BuyProductTitle.gravity = Gravity.CENTER_VERTICAL;
                        text_BuyProductsTitle.setLayoutParams(layoutParams_BuyProductTitle);
                        text_BuyProductsTitle.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                        text_BuyProductsTitle.setTextSize(mTextViewSize);
                        text_BuyProductsTitle.setMaxLines(2);

                        if (bModel.labelsMasterHelper.applyLabels(BUY_PRODUCT_TITLE_LABEL) != null)
                            text_BuyProductsTitle.setText(bModel.labelsMasterHelper.applyLabels(BUY_PRODUCT_TITLE_LABEL));
                        else
                            text_BuyProductsTitle.setText(mContext.getResources().getString(R.string.buy));

                        text_BuyProductsTitle.setWidth(mProductNameWidth);
                        text_BuyProductsTitle.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        text_BuyProductsTitle.setGravity(Gravity.LEFT | Gravity.CENTER);
                        text_BuyProductsTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        text_BuyProductsTitle.setMaxLines(2);

                        LinearLayout layout_Slab;
                        layout_Slab = new LinearLayout(mContext);
                        layout_Slab.setOrientation(LinearLayout.HORIZONTAL);
                        layout_Slab.setLayoutParams(layoutParams_BuyProductTitle);
                        layout_Slab.addView(text_BuyProductsTitle);

                        LinearLayout.LayoutParams layoutParams_slab = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams_slab.gravity = Gravity.CENTER_VERTICAL;


                        ArrayList<String> mSlabList = schemeHelper.getSchemeIdListByParentID().get(parentId);
                        if (mSlabList != null) {
                            for (int k = mSlabList.size() - 1; k >= 0; k--) {

                                //if(mSelectedSlabId.equals("0")||mSlabList.get(k).equals(mSelectedSlabId)) {

                                final TextView slab = getTextView(true, Gravity.CENTER, false);
                                slab.setLayoutParams(layoutParams_slab);
                                slab.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                                slab.setTextSize(mTextViewSize);
                                slab.setEllipsize(TextUtils.TruncateAt.END);
                                slab.setMaxLines(2);
                                if (schemeHelper.getSchemeById().get(mSlabList.get(k)).getScheme() != null) {
                                    slab.setText(schemeHelper.getSchemeById().get(mSlabList.get(k)).getScheme());
                                    slab.setTag(schemeHelper.getSchemeById().get(mSlabList.get(k)).getScheme());
                                }
                                slab.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                                slab.setWidth(mSchemeDetailWidth);
                                slab.setTextColor(getResources().getColor(R.color.FullBlack));
                                layout_Slab.addView(slab);


                                slab.setOnTouchListener(new View.OnTouchListener() {
                                    private int CLICK_ACTION_THRESHOLD = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
                                    private float startX;
                                    private float startY;
                                    int xvalue, yvalue;
                                    String slabText = "";

                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        switch (event.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                startX = event.getX();
                                                startY = event.getY();
                                                Log.i("TAG", "touched down");
                                                break;
                                            case MotionEvent.ACTION_MOVE:
                                                break;
                                            case MotionEvent.ACTION_UP:

                                                float endX = event.getX();
                                                float endY = event.getY();
                                                if (isAClick(startX, endX, startY, endY)) {
                                                    xvalue = (int) event.getRawX();
                                                    yvalue = (int) event.getRawY();
                                                    slabText = v.getTag().toString();
                                                    showPopupWindow(xvalue, yvalue, slabText);
                                                    Log.i("TAG", "touched up");
                                                }
                                                break;
                                        }
                                        return true;
                                    }

                                    // handle to perform like click event
                                    private boolean isAClick(float startX, float endX, float startY, float endY) {
                                        float differenceX = Math.abs(startX - endX);
                                        float differenceY = Math.abs(startY - endY);
                                        return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD);
                                    }

                                });
                                //}
                            }
                        }

                        layout_BuyProductParent.addView(layout_Slab);
                        mainLayout.addView(layout_BuyProductParent);

                        int i = 0;
                        for (SchemeProductBO buyProductBO : schemeBuyProducts) {
                            if (!groupName.contains(buyProductBO.getGroupName())) {
                                i = i + 1;
                                if (i > 1) {

                                    TextView groupLogicType = getTextView(false, Gravity.LEFT, false);
                                    groupLogicType.setTextColor(getResources().getColor(R.color.FullBlack));

                                    if (schemeBO.getParentLogic().equalsIgnoreCase("AND")) {
                                        groupLogicType.setText("&");
                                    } else if (schemeBO.getParentLogic().equalsIgnoreCase("ANY")) {
                                        groupLogicType.setText("OR");
                                    } else {
                                        groupLogicType.setText(schemeBO.getParentLogic());
                                    }

                                    groupLogicType.setTypeface(null, Typeface.ITALIC);
                                    mainLayout.addView(groupLogicType);
                                }

                                if (buyProductBO.getGroupLogic().equals(AND_LOGIC)) {
                                    final HorizontalScrollView ll = addViewAndLogicBUY(parentId, buyProductBO.getGroupName());
                                    mainLayout.addView(ll);

                                } else if (buyProductBO.getGroupLogic().equals(ANY_LOGIC)) {
                                    final HorizontalScrollView ll = addViewANYLogicBUY(parentId, buyProductBO.getGroupName());
                                    mainLayout.addView(ll);

                                } else if (buyProductBO.getGroupLogic().equals(ONLY_LOGIC)) {
                                    final HorizontalScrollView ll = addViewONLYLogicBuy(parentId, buyProductBO.getGroupName());
                                    mainLayout.addView(ll);
                                }


                            }

                            groupName.add(buyProductBO.getGroupName());

                        }
                    }


                    // Adding Discounts view ///////////
                    boolean isAlreadyAddedFreeProduct = false;
                    boolean isAlreadyAddedOtherDisc = false;

                    boolean isSameGroupAvailableInOtherSlab = schemeHelper.isSameGroupAvailableInOtherSlab(parentId);
                    if (!isSameGroupAvailableInOtherSlab) {
                        // If same group not available in other slab, then single title is enough

                        TextView freeTitleTV = getTextView(true, Gravity.LEFT, true);
                        freeTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                        freeTitleTV.setTextColor(getResources().getColor(R.color.FullBlack));
                        freeTitleTV.setTextSize(mTextViewSize);

                        if (bModel.labelsMasterHelper.applyLabels(FREE_PRODUCT_TITLE_LABEL) != null)
                            freeTitleTV.setText(bModel.labelsMasterHelper.applyLabels(FREE_PRODUCT_TITLE_LABEL));
                        else
                            freeTitleTV.setText("GET FreeProduct");

                        freeTitleTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        freeTitleTV.setWidth(150);

                        LinearLayout.LayoutParams layoutParam_FreeTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParam_FreeTitle.setMargins(0, 5, 0, 5);
                        freeTitleTV.setLayoutParams(layoutParam_FreeTitle);
                        freeTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        freeTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);

                        mainLayout.addView(freeTitleTV);
                    }


                    for (String schemeId : schemeIdList) {

                        final SchemeBO slabBO = schemeHelper.getSchemeById().get(schemeId);
                        List<SchemeProductBO> schemeFreeList = slabBO.getFreeProducts();

                        if (isSameGroupAvailableInOtherSlab) {
                            // Same group available in other slab

                            if (schemeFreeList != null && schemeFreeList.size() > 0 && !isAlreadyAddedFreeProduct) {

                                int i = 0;
                                for (SchemeProductBO freeProductBO : schemeFreeList) {
                                    if (freeProductBO.getGroupName() != null) {

                                        if (!groupName.contains(schemeId + freeProductBO.getGroupName())) {
                                            if (i == 0) {

                                                isAlreadyAddedFreeProduct = true;

                                                TextView freeTitleTV = getTextView(true, Gravity.LEFT, true);
                                                freeTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                                                freeTitleTV.setTextColor(getResources().getColor(R.color.FullBlack));
                                                freeTitleTV.setTextSize(mTextViewSize);

                                                if (bModel.labelsMasterHelper.applyLabels(FREE_PRODUCT_TITLE_LABEL) != null)
                                                    freeTitleTV.setText(bModel.labelsMasterHelper.applyLabels(FREE_PRODUCT_TITLE_LABEL));
                                                else
                                                    freeTitleTV.setText("GET FreeProduct");

                                                freeTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);
                                                freeTitleTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                                                freeTitleTV.setWidth(150);

                                                LinearLayout.LayoutParams layoutParam_FreeTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                                                layoutParam_FreeTitle.setMargins(0, 10, 0, 10);
                                                freeTitleTV.setLayoutParams(layoutParam_FreeTitle);
                                                freeTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

                                                mainLayout.addView(freeTitleTV);
                                            }
                                            i++;

                                            if (i > 1) {

                                                TextView groupLogicType = getTextView(false, Gravity.LEFT, false);
                                                groupLogicType.setTextColor(getResources().getColor(R.color.FullBlack));

                                                if (schemeBO.getFreeType().equalsIgnoreCase("AND")) {
                                                    groupLogicType.setText("&");
                                                } else if (schemeBO.getFreeType().equalsIgnoreCase("ANY")) {
                                                    groupLogicType.setText("OR");
                                                } else {
                                                    groupLogicType.setText(schemeBO.getFreeType());
                                                }

                                                groupLogicType.setTypeface(null, Typeface.ITALIC);

                                                mainLayout.addView(groupLogicType);
                                            }


                                            //Adding  free products
                                            if (freeProductBO.getGroupLogic().equals(AND_LOGIC)) {
                                                final HorizontalScrollView ll = addViewANDLogicGET(parentId, freeProductBO.getGroupName());
                                                mainLayout.addView(ll);
                                            } else if (freeProductBO.getGroupLogic().equals(ANY_LOGIC)) {
                                                final HorizontalScrollView ll = addViewANYLogicGET(parentId, freeProductBO.getGroupName());
                                                mainLayout.addView(ll);
                                            } else if (freeProductBO.getGroupLogic().equals(ONLY_LOGIC)) {

                                                final HorizontalScrollView ll = addViewONLYLogicGET(parentId, freeProductBO.getGroupName());
                                                mainLayout.addView(ll);

                                            }
                                        }
                                        groupName.add(schemeId + freeProductBO.getGroupName());
                                    }
                                }

                            }
                        } else {
                            // Same group not available in other slab
                            TextView slabNameTV = getDefaultTextView();
                            slabNameTV.setText(slabBO.getScheme());
                            mainLayout.addView(slabNameTV);

                            ArrayList<String> groupNameList = schemeHelper.getFreeGroupNameListBySchemeID().get(schemeId);
                            if (groupNameList != null) {

                                int j = 1;
                                for (String grpName : groupNameList) {
                                    String groupLogic = schemeHelper.getGroupBuyTypeByGroupName().get(schemeId + grpName);

                                    if (groupLogic != null) {

                                        if (j > 1) {

                                            TextView groupLogicType = getTextView(false, Gravity.CENTER, false);

                                            groupLogicType.setText(slabBO.getFreeType());
                                            groupLogicType.setTypeface(null, Typeface.ITALIC);
                                            mainLayout.addView(groupLogicType);
                                        }

                                        //Adding free products
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

                        //Adding free discounts(Except free products)
                        if (schemeFreeList != null && schemeFreeList.size() > 0 && !isAlreadyAddedOtherDisc) {

                            SchemeProductBO freeProductBO = schemeFreeList.get(0);
                            if (freeProductBO != null && (freeProductBO.getMaxPercent() > 0 || freeProductBO.getMaxAmount() > 0 || freeProductBO.getPriceMaximum() > 0)) {

                                TextView freeTitleTV = getTextView(true, Gravity.LEFT, true);
                                freeTitleTV.setBackgroundColor(getResources().getColor(R.color.scheme_title_grey));
                                freeTitleTV.setTextSize(mTextViewSize);
                                freeTitleTV.setText("GET Discount");
                                freeTitleTV.setWidth(150);
                                freeTitleTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                layoutParams1.setMargins(0, 20, 0, 10);
                                freeTitleTV.setLayoutParams(layoutParams1);
                                freeTitleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                freeTitleTV.setGravity(Gravity.LEFT | Gravity.CENTER);
                                mainLayout.addView(freeTitleTV);

                                isAlreadyAddedOtherDisc = true;

                                final HorizontalScrollView ll = addViewSchemeDiscounts(parentId);
                                mainLayout.addView(ll);


                            }
                        }

                    }

                    LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30);
                    lineLayoutParams.setMargins(0, 5, 0, 5);
                    lineLayoutParams.gravity = Gravity.CENTER;
                    LinearLayout lineLayouts = new LinearLayout(mContext);
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

        int maximumLineCount = schemeHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);

        if (maximumLineCount == 0)
            maximumLineCount = 1;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        LinearLayout.LayoutParams layoutParams_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams_parent.setMargins(0, 5, 0, 0);
        layoutParams_parent.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParams_parent);

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = schemeHelper.getSchemeIdListByParentID().get(schemeParentId);
        final int size = schemeList.size();

        if (size > 0) {

            LinearLayout childHeaderView = new LinearLayout(mContext);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getDefaultTextView();
            txt.setMaxLines(maximumLineCount);
            txt.setHeight(maximumLineCount * mTextViewHeight);
            if (schemeHelper.IS_SCHEME_SLAB_ON)
                txt.setText("SLAB");
            else
                txt.setText("SKU");

            childHeaderView.addView(txt);

            SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeList.get(size - 1));
            //Adding buy products to the view
            if (schemeBO != null) {
                List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();

                if (schemeHelper.IS_SCHEME_SLAB_ON) {

                    TextView productTV = getTextView(false, Gravity.LEFT, false);
                    productTV.setText(R.string.qty);
                    productTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    productTV.setTextColor(getResources().getColor(R.color.FullBlack));
                    productTV.setWidth(mProductNameWidth);
                    childHeaderView.addView(productTV);

                } else {

                    for (SchemeProductBO schemeProductBO : buyProductList) {

                        if (groupName.equals(schemeProductBO.getGroupName())) {

                            // ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            //if (productBO != null) {

                            TextView productTV = getTextView(false, Gravity.LEFT, false);
                            productTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            productTV.setTextColor(getResources().getColor(R.color.FullBlack));
                            productTV.setText(schemeProductBO.getProductName());
                            productTV.setWidth(mProductNameWidth);
                            childHeaderView.addView(productTV);
                            // }
                        }
                    }
                }
            }

            layout_parent.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = schemeHelper.getSchemeById().get(schemeList.get(i));

                if (schemeBO != null) {

                    TextView schemeNameTV = getDefaultTextView();
                    schemeNameTV.setText(schemeBO.getScheme());
                    schemeNameTV.setWidth(mSchemeDetailWidth);
                    schemeNameTV.setMaxLines(maximumLineCount);
                    schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);

                    LinearLayout schemeChildView = new LinearLayout(mContext);
                    schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    schemeChildView.setOrientation(LinearLayout.VERTICAL);
                    schemeChildView.addView(schemeNameTV);


                    List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();
                    for (SchemeProductBO schemeProductBO : buyProductList) {
                        if (groupName.equals(schemeProductBO.getGroupName())) {

                            TextView tv = getDefaultTextView();
                            tv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            tv.setTextColor(getResources().getColor(R.color.FullBlack));
                            tv.setWidth(mSchemeDetailWidth);

                            //  ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            //  if (productBO != null) {

                            if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                tv.setText(SDUtil.getWithoutExponential(schemeProductBO.getBuyQty()) + "-" + SDUtil.getWithoutExponential(schemeProductBO.getTobuyQty()) + " " + schemeProductBO.getUomDescription());

                            } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                tv.setText(SDUtil.getWithoutExponential(schemeProductBO.getBuyQty()) + "-" + SDUtil.getWithoutExponential(schemeProductBO.getTobuyQty()) + " " + rupeesLabel);
                            }

                            schemeChildView.addView(tv);
                            break;
                            //  }

                        }


                    }
                    layout_parent.addView(schemeChildView);


                }
            }


        }
        horizontalScrollView.addView(layout_parent);

        return horizontalScrollView;
    }

    private HorizontalScrollView addViewONLYLogicBuy(int schemeParentId, String groupName) {

        int maximumLineCount = schemeHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        if (maximumLineCount == 0)
            maximumLineCount = 1;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        LinearLayout.LayoutParams layoutParam_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParam_parent.setMargins(0, 5, 0, 0);
        layoutParam_parent.gravity = Gravity.LEFT;
        horizontalScrollView.setLayoutParams(layoutParam_parent);

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = schemeHelper.getSchemeIdListByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (size > 0) {

            SchemeBO schemeBO;
            LinearLayout childHeaderView = new LinearLayout(mContext);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getDefaultTextView();
            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = schemeHelper.getSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {

                List<SchemeProductBO> buyProdList = schemeBO.getBuyingProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {

                    if (groupName.equals(schemeProductBO.getGroupName())) {

                        TextView productTV = getTextView(false, Gravity.LEFT, false);
                        productTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        productTV.setTextColor(Color.BLACK);
                        // ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        // if (productBO != null) {
                        productTV.setText(schemeProductBO.getProductName());
                        // }
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(productTV);
                    }
                }
            }


            layout_parent.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {

                schemeBO = schemeHelper.getSchemeById().get(schemeList.get(i));

                LinearLayout.LayoutParams detailLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
                detailLayout.weight = 0.1f;
                detailLayout.gravity = Gravity.CENTER_VERTICAL;

                if (schemeBO != null) {

                    TextView schemeNameTV = getDefaultTextView();
                    schemeNameTV.setText(schemeBO.getScheme());
                    schemeNameTV.setWidth(mSchemeDetailWidth);
                    schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);

                    LinearLayout schemeChildView = new LinearLayout(mContext);
                    schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    schemeChildView.setOrientation(LinearLayout.VERTICAL);
                    schemeChildView.addView(schemeNameTV);

                    List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();
                    for (SchemeProductBO schemeProductBO : buyProductList) {

                        if (groupName.equals(schemeProductBO.getGroupName())) {

                            TextView tv = getDefaultTextView();
                            tv.setTextColor(Color.BLACK);

                            //ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                            //if (productBO != null) {

                            if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {
                                tv.setText(SDUtil.getWithoutExponential(schemeProductBO.getBuyQty()) + "-" + SDUtil.getWithoutExponential(schemeProductBO.getTobuyQty()) + " " + schemeProductBO.getUomDescription());

                            } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {
                                tv.setText(SDUtil.getWithoutExponential(schemeProductBO.getBuyQty()) + "-" + SDUtil.getWithoutExponential(schemeProductBO.getTobuyQty()) + " " + rupeesLabel);
                            }
                            tv.setWidth(mSchemeDetailWidth);
                            tv.setSingleLine(true);


                            //}
                            tv.setLayoutParams(detailLayout);
                            schemeChildView.addView(tv);
                        }
                    }
                    layout_parent.addView(schemeChildView);


                }
            }


        }

        horizontalScrollView.addView(layout_parent);
        return horizontalScrollView;
    }

    private HorizontalScrollView addViewAndLogicBUY(int parentId, String groupName) {

        LinearLayout mAddViewHorizontalLayout;

        int maximumLineCount = schemeHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, parentId);
        if (maximumLineCount == 0)
            maximumLineCount = 1;

        LinearLayout.LayoutParams layoutParams_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams_parent.setMargins(0, 5, 0, 0);
        layoutParams_parent.gravity = Gravity.LEFT;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        horizontalScrollView.setLayoutParams(layoutParams_parent);

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        final ArrayList<String> productIdList = schemeHelper.getProductIdListByParentId().get(parentId);
        if (productIdList != null) {

            final ArrayList<String> schemeIdList = schemeHelper.getSchemeIdListByParentID().get(parentId);
            int size = schemeIdList.size();
            int j = 0;

            for (String productId : productIdList) {

                LinearLayout headerLayout = new LinearLayout(mContext);
                headerLayout.setOrientation(LinearLayout.HORIZONTAL);
                headerLayout.setLayoutParams(layoutParams_parent);
                mAddViewHorizontalLayout = new LinearLayout(mContext);
                mAddViewHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                mAddViewHorizontalLayout.setLayoutParams(layoutParams_parent);

                //   ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(productId);
                //    if (productBO != null) {

                if (j == 0) {

                    TextView headerTV = getTextView(false, Gravity.CENTER, false);
                    headerTV.setText("SKU");
                    headerTV.setWidth(mProductNameWidth);
                    headerTV.setHeight(maximumLineCount * mTextViewHeight);
                    headerLayout.addView(headerTV);
                }

                for (int i = size - 1; i >= 0; i--) {

                    String schemeId = schemeIdList.get(i);
                    SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeId);
                    if (schemeBO != null) {
                        if (j == 0) {

                            TextView schemeNameTV = getDefaultTextView();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);
                            headerLayout.addView(schemeNameTV);

                        }


                        final SchemeProductBO buyProductBO = schemeHelper.getBuyProductBOBySchemeIdWithPid().get(schemeId + productId);
                        if (buyProductBO != null && buyProductBO.getGroupName().equals(groupName)) {

                            if (i == size - 1) {

                                TextView text_ProductName = getTextView(false, Gravity.LEFT, false);
                                text_ProductName.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                text_ProductName.setTextColor(Color.BLACK);
                                text_ProductName.setText(buyProductBO.getProductName());
                                text_ProductName.setWidth(mProductNameWidth);
                                mAddViewHorizontalLayout.addView(text_ProductName);
                            }

                            TextView schemeDetailsTV = getDefaultTextView();
                            StringBuffer sb = new StringBuffer();

                            if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_QTY)) {

                                if (buyProductBO.getBuyQty() == buyProductBO.getTobuyQty()) {
                                    sb.append(SDUtil.getWithoutExponential(buyProductBO.getBuyQty()));
                                } else {
                                    sb.append(SDUtil.getWithoutExponential(buyProductBO.getBuyQty()) + " - " + SDUtil.getWithoutExponential(buyProductBO.getTobuyQty()));
                                }
                                sb.append(" " + buyProductBO.getUomDescription());

                            } else if (schemeBO.getBuyType().equals(SCHEME_BUY_TYPE_VALUE)) {

                                if (buyProductBO.getBuyQty() == buyProductBO.getTobuyQty()) {
                                    sb.append(SDUtil.getWithoutExponential(buyProductBO.getBuyQty()));
                                } else {
                                    sb.append(SDUtil.getWithoutExponential(buyProductBO.getBuyQty()) + " - " + SDUtil.getWithoutExponential(buyProductBO.getTobuyQty()));
                                }
                                sb.append(" " + rupeesLabel);
                            }

                            schemeDetailsTV.setTextColor(getResources().getColor(R.color.FullBlack));
                            schemeDetailsTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            schemeDetailsTV.setText(sb.toString());
                            schemeDetailsTV.setWidth(mSchemeDetailWidth);
                            mAddViewHorizontalLayout.addView(schemeDetailsTV);

                        }
                    }


                }

                if (j == 0) {
                    layout_parent.addView(headerLayout);
                }

                layout_parent.addView(mAddViewHorizontalLayout);
                j++;


                //}


            }
        }
        horizontalScrollView.addView(layout_parent);
        return horizontalScrollView;

    }

    private HorizontalScrollView addViewANDLogicGET(int schemeParentId, String groupName) {
        int maximumLineCount = schemeHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);

        if (maximumLineCount == 0)
            maximumLineCount = 1;

        LinearLayout.LayoutParams layoutParam_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParam_parent.bottomMargin = 10;
        layoutParam_parent.gravity = Gravity.LEFT | Gravity.CENTER;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        horizontalScrollView.setLayoutParams(layoutParam_parent);

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = schemeHelper.getSchemeIdListByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (size > 0) {

            SchemeBO schemeBO;

            LinearLayout childHeaderView = new LinearLayout(mContext);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getDefaultTextView();
            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = schemeHelper.getSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {

                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {

                        TextView productTV = getTextView(false, Gravity.LEFT, false);
                        productTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                        ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {

                            productTV.setText(productBO.getProductShortName());
                            productTV.setWidth(mProductNameWidth);
                            childHeaderView.addView(productTV);
                        }

                    }
                }
            }


            layout_parent.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {

                schemeBO = schemeHelper.getSchemeById().get(schemeList.get(i));
                if (schemeBO != null) {

                    List<SchemeProductBO> buyProductList = schemeBO.getFreeProducts();
                    if (buyProductList != null && buyProductList.size() > 0) {

                        SchemeProductBO freeProductBO = buyProductList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {

                            TextView text_schemeName = getDefaultTextView();
                            text_schemeName.setText(schemeBO.getScheme());
                            text_schemeName.setWidth(mSchemeDetailWidth);
                            text_schemeName.setHeight(maximumLineCount * mTextViewHeight);

                            LinearLayout schemeChildView = new LinearLayout(mContext);
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            schemeChildView.addView(text_schemeName);

                            for (SchemeProductBO schemeProductBO : buyProductList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {

                                    TextView tv = getDefaultTextView();
                                    tv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));

                                    ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
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

                            layout_parent.addView(schemeChildView);

                        }
                    }
                }
            }


        }

        horizontalScrollView.addView(layout_parent);
        return horizontalScrollView;
    }


    private HorizontalScrollView addViewANYLogicGET(int schemeParentId, String groupName) {

        int maximumLineCount = schemeHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        if (maximumLineCount == 0)
            maximumLineCount = 1;

        LinearLayout.LayoutParams layoutParams_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams_parent.bottomMargin = 20;
        layoutParams_parent.gravity = Gravity.LEFT;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        horizontalScrollView.setLayoutParams(layoutParams_parent);

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.HORIZONTAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = schemeHelper.getSchemeIdListByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (size > 0) {

            SchemeBO schemeBO;

            LinearLayout childHeaderView = new LinearLayout(mContext);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getDefaultTextView();
            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = schemeHelper.getSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {

                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();

                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {

                        TextView productTV = getTextView(false, Gravity.LEFT, false);

                        productTV.setTextColor(Color.BLACK);
                        productTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        productTV.setText(schemeProductBO.getProductName());
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(productTV);


                    }
                }

            }

            layout_parent.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = schemeHelper.getSchemeById().get(schemeList.get(i));
                if (schemeBO != null) {

                    List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                    if (freeProductList != null && freeProductList.size() > 0) {

                        SchemeProductBO freeProductBO = freeProductList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {

                            TextView schemeNameTV = getDefaultTextView();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);

                            LinearLayout schemeChildView = new LinearLayout(mContext);
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : freeProductList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {

                                    TextView tv = getDefaultTextView();
                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                    tv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                                    //   ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    //    if (productBO != null) {

                                    tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                    tv.setWidth(mSchemeDetailWidth);

                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                    tv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                    schemeChildView.addView(tv);
                                    break;
                                    //  }

                                }


                            }
                            layout_parent.addView(schemeChildView);


                        }
                    }
                }
            }


        }

        horizontalScrollView.addView(layout_parent);
        return horizontalScrollView;
    }

    private HorizontalScrollView addViewONLYLogicGET(int schemeParentId, String groupName) {

        int maximumLineCount = schemeHelper.getMaximumLineOfSchemeHeight(mSchemeDetailWidth, schemeParentId);
        if (maximumLineCount == 0)
            maximumLineCount = 1;

        LinearLayout.LayoutParams layoutParams_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams_parent.bottomMargin = 20;
        layoutParams_parent.gravity = Gravity.LEFT;

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        horizontalScrollView.setLayoutParams(layoutParams_parent);


        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        ArrayList<String> schemeList = schemeHelper.getSchemeIdListByParentID().get(schemeParentId);
        final int size = schemeList.size();
        if (size > 0) {

            SchemeBO schemeBO;

            LinearLayout childHeaderView = new LinearLayout(mContext);
            childHeaderView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            childHeaderView.setOrientation(LinearLayout.VERTICAL);

            TextView txt = getDefaultTextView();
            txt.setText("SKU");
            txt.setHeight(maximumLineCount * mTextViewHeight);

            childHeaderView.addView(txt);

            schemeBO = schemeHelper.getSchemeById().get(schemeList.get(size - 1));
            if (schemeBO != null) {

                List<SchemeProductBO> buyProdList = schemeBO.getFreeProducts();
                for (SchemeProductBO schemeProductBO : buyProdList) {
                    if (groupName.equals(schemeProductBO.getGroupName())) {

                        TextView productTV = getTextView(false, Gravity.LEFT, false);
                        productTV.setTextColor(Color.BLACK);
                        productTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        productTV.setText(schemeProductBO.getProductName());
                        productTV.setWidth(mProductNameWidth);
                        childHeaderView.addView(productTV);
                    }
                }
            }


            layout_parent.addView(childHeaderView);

            for (int i = size - 1; i >= 0; i--) {


                schemeBO = schemeHelper.getSchemeById().get(schemeList.get(i));
                if (schemeBO != null) {

                    List<SchemeProductBO> freeProductsList = schemeBO.getFreeProducts();
                    if (freeProductsList != null && freeProductsList.size() > 0) {

                        SchemeProductBO freeProductBO = freeProductsList.get(0);
                        if (freeProductBO.getProductId() != null && !freeProductBO.getProductId().equals("")) {

                            TextView schemeNameTV = getDefaultTextView();
                            schemeNameTV.setText(schemeBO.getScheme());
                            schemeNameTV.setWidth(mSchemeDetailWidth);
                            schemeNameTV.setHeight(maximumLineCount * mTextViewHeight);

                            LinearLayout schemeChildView = new LinearLayout(mContext);
                            schemeChildView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            schemeChildView.setOrientation(LinearLayout.VERTICAL);
                            schemeChildView.addView(schemeNameTV);

                            for (SchemeProductBO schemeProductBO : freeProductsList) {
                                if (groupName.equals(schemeProductBO.getGroupName())) {

                                    TextView tv = getDefaultTextView();

                                    tv.setText(schemeProductBO.getQuantityMinimum() + "-" + schemeProductBO.getQuantityMaximum() + " " + schemeProductBO.getUomDescription());
                                    tv.setWidth(mSchemeDetailWidth);
                                    tv.setSingleLine(true);

                                    tv.setTextColor(getResources().getColor(R.color.FullBlack));
                                    tv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                                    schemeChildView.addView(tv);
                                }
                            }
                            layout_parent.addView(schemeChildView);
                        }
                    }

                }
            }

        }

        horizontalScrollView.addView(layout_parent);
        return horizontalScrollView;
    }


    private HorizontalScrollView addViewSchemeDiscounts(int mParentId) {

        LinearLayout.LayoutParams layoutParams_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        LinearLayout childView1 = null;
        LinearLayout childView2 = null;
        LinearLayout childView3 = null;
        layoutParams_parent.setMargins(0, 5, 0, 0);
        layoutParams_parent.gravity = Gravity.START;
        horizontalScrollView.setLayoutParams(layoutParams_parent);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.border));
        } else {
            layout_parent.setBackground(mContext.getResources().getDrawable(R.drawable.border));
        }


        ArrayList<String> schemeList = schemeHelper.getSchemeIdListByParentID().get(mParentId);
        boolean isPercentageDiscAvailable = false;
        boolean isAmountDiscAvailable = false;
        boolean isPriceDiscAvailable = false;
        for (String schemeId : schemeList) {
            final SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeId);
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

        LinearLayout headerView = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParamsMatchParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(layoutParamsMatchParent);
        headerView.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = getTextView(false, Gravity.CENTER, false);
        tv.setText("Type");
        tv.setWidth(mSchemeDetailWidth);
        tv.setLayoutParams(layoutParams);


        for (int i = size - 1; i >= 0; i--) {
            SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeList.get(i));
            if (schemeBO != null) {

                TextView schemeNameTV = getTextView(false, Gravity.CENTER, false);
                schemeNameTV.setText(schemeBO.getScheme());
                schemeNameTV.setLayoutParams(layoutParamsMatchParent);
                schemeNameTV.setWidth(mSchemeDetailWidth);
                if (i == 0) {
                    layout_parent.addView(headerView);
                }

                final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                if (freeProductList != null && freeProductList.size() > 0) {
                    SchemeProductBO freeProductBO = freeProductList.get(0);

                    if (isAmountDiscAvailable) {
                        if (childView1 == null) {
                            childView1 = new LinearLayout(mContext);
                            childView1.setOrientation(LinearLayout.HORIZONTAL);
                            childView1.setLayoutParams(layoutParams);
                            TextView amountTitleTV = getTextView(false, Gravity.LEFT, false);
                            amountTitleTV.setText("Amount");
                            amountTitleTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            amountTitleTV.setTextColor(Color.BLACK);
                            amountTitleTV.setWidth(mProductNameWidth);
                            amountTitleTV.setLayoutParams(layoutParams);
                            childView1.addView(amountTitleTV);
                        }

                        TextView amountTV = getTextView(false, Gravity.CENTER, false);
                        amountTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        amountTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.FullBlack));
                        amountTV.setWidth(mSchemeDetailWidth);

                        if (freeProductBO.getMinAmount() == freeProductBO.getMaxAmount()) {
                            amountTV.setText(bModel.formatValue(freeProductBO.getMinAmount()));

                        } else {
                            amountTV.setText(bModel.formatValue(freeProductBO.getMinAmount()) + " - " + bModel.formatValue(freeProductBO.getMaxAmount()));
                        }

                        amountTV.setLayoutParams(layoutParams);
                        childView1.addView(amountTV);

                        if (i == 0) {
                            layout_parent.addView(childView1);
                        }


                    }
                    if (isPercentageDiscAvailable) {
                        if (childView2 == null) {
                            childView2 = new LinearLayout(mContext);
                            childView2.setOrientation(LinearLayout.HORIZONTAL);
                            childView2.setLayoutParams(layoutParams);
                            TextView distTitleTV = getTextView(false, Gravity.LEFT, false);
                            distTitleTV.setTextColor(Color.BLACK);
                            distTitleTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            distTitleTV.setText("%Disc");
                            distTitleTV.setWidth(mProductNameWidth);
                            childView2.addView(distTitleTV);
                        }

                        TextView percentTV = getTextView(false, Gravity.CENTER, false);
                        percentTV.setTextColor(Color.BLACK);

                        if (freeProductBO.getMinPercent() == freeProductBO.getMaxPercent()) {
                            percentTV.setText(freeProductBO.getMinPercent() + "");
                        } else {
                            percentTV.setText(freeProductBO.getMinPercent() + " - " + freeProductBO.getMaxPercent());
                        }

                        percentTV.setLayoutParams(layoutParams);
                        percentTV.setWidth(mSchemeDetailWidth);
                        childView2.addView(percentTV);
                        if (i == 0) {
                            layout_parent.addView(childView2);
                        }


                    }
                    if (isPriceDiscAvailable) {
                        if (childView3 == null) {
                            childView3 = new LinearLayout(mContext);
                            childView3.setOrientation(LinearLayout.HORIZONTAL);

                            TextView priceTitleTV = getTextView(false, Gravity.LEFT, false);
                            priceTitleTV.setTextColor(Color.BLACK);
                            priceTitleTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            priceTitleTV.setText("Price");
                            priceTitleTV.setLayoutParams(layoutParams);
                            priceTitleTV.setWidth(mProductNameWidth);
                            childView3.addView(priceTitleTV);
                        }

                        TextView priceTV = getTextView(false, Gravity.CENTER, false);
                        priceTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.FullBlack));
                        priceTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (freeProductBO.getPriceActual() > 0) {
                            if (freeProductBO.getPriceActual() == freeProductBO.getPriceMaximum()) {
                                priceTV.setText(String.valueOf(freeProductBO.getPriceActual()));
                            } else {
                                priceTV.setText(freeProductBO.getPriceActual() + " - " + freeProductBO.getPriceMaximum());
                            }
                        } else {
                            priceTV.setText("-");
                        }
                        priceTV.setLayoutParams(layoutParams);
                        priceTV.setWidth(mSchemeDetailWidth);
                        childView3.addView(priceTV);
                        if (i == 0) {
                            layout_parent.addView(childView3);
                        }
                    }


                }

            }
        }
        horizontalScrollView.addView(layout_parent);
        return horizontalScrollView;
    }


    private TextView getTextView(boolean isBackGround, int gravity, boolean isTextColor) {

        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView textView = new TextView(mContext);
        textView.setLayoutParams(layoutParam);
        textView.setGravity(gravity);
        textView.setPadding(10, 4, 0, 4);
        textView.setBackgroundColor(mContext.getResources().getColor(R.color.white));

        if (isBackGround)
            textView.setBackgroundColor(mContext.getResources().getColor(R.color.highlighter));
        if (isTextColor)
            textView.setTextColor(mContext.getResources().getColor(R.color.highlighter));


        return textView;
    }

    private TextView getDefaultTextView() {
        final TextView textView = new TextView(getActivity());
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(mTextViewSize);
        textView.setPadding(10, 4, 0, 4);
        textView.setSingleLine(false);


        return textView;
    }

    private TextView getHorizontalLine() {
        TextView horizontalLine = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((mTotalScreenWidth * 97) / (100), 1);
        horizontalLine.setLayoutParams(layoutParams);
        horizontalLine.setBackgroundColor(mContext.getResources().getColor(R.color.gray_text));
        horizontalLine.setPadding(0, 20, 0, 20);
        horizontalLine.setGravity(Gravity.CENTER);
        return horizontalLine;
    }


    private LinearLayout addSlabWiseFreeProductANDLogic(String schemeId, String groupName) {


        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        LinearLayout layout_child = new LinearLayout(mContext);
        layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        layout_child.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = getDefaultTextView();
        txt.setWidth(mSlabWiseProductNameWidth);
        txt.setText("SKU");

        layout_child.addView(txt);

        TextView schemeNameTV = getDefaultTextView();
        SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeId);
        schemeNameTV.setText(schemeBO.getScheme());
        schemeNameTV.setWidth(mSlabWiseSchemeNameWidth);

        layout_child.addView(schemeNameTV);

        layout_parent.addView(layout_child);

        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        for (SchemeProductBO freeProductBO : freeProductList) {
            if (groupName.equals(freeProductBO.getGroupName())) {

                ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(freeProductBO.getProductId());
                if (productBO != null) {

                    layout_child = new LinearLayout(mContext);
                    layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    layout_child.setOrientation(LinearLayout.HORIZONTAL);

                    TextView text_ProductName = getDefaultTextView();
                    text_ProductName.setText(productBO.getProductShortName());
                    text_ProductName.setWidth(mSlabWiseProductNameWidth);
                    layout_child.addView(text_ProductName);

                    String freeQty;
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

                    TextView slabValueTV = getDefaultTextView();
                    slabValueTV.setWidth(mSlabWiseSchemeNameWidth);
                    slabValueTV.setText(freeQty);

                    layout_child.addView(slabValueTV);
                    layout_parent.addView(layout_child);
                }

            }
        }


        return layout_parent;


    }

    private LinearLayout addSlabWiseFreeProductANYLogic(String schemeId, String groupName) {

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        LinearLayout layout_child = new LinearLayout(mContext);
        layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        layout_child.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = getDefaultTextView();
        txt.setText("SKU");
        txt.setWidth(mSlabWiseProductNameWidth);

        layout_child.addView(txt);

        TextView schemeNameTV = getDefaultTextView();
        SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeId);
        schemeNameTV.setText(schemeBO.getScheme());
        schemeNameTV.setWidth(mSlabWiseSchemeNameWidth);

        layout_child.addView(schemeNameTV);
        layout_parent.addView(layout_child);

        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        StringBuffer sb = new StringBuffer();

        layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        layout_child.setOrientation(LinearLayout.VERTICAL);

        TextView productNameTV = getDefaultTextView();
        String fromQty = "", toQty = "";
        String uomDes = "";

        for (SchemeProductBO freeProductBO : freeProductList) {
            if (freeProductBO.getGroupName().equals(groupName)) {

                ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(freeProductBO.getProductId());
                if (productBO != null) {

                    sb.append(productBO.getProductShortName());
                    sb.append("\n");

                    fromQty = SDUtil.getWithoutExponential(freeProductBO.getBuyQty());
                    toQty = SDUtil.getWithoutExponential(freeProductBO.getTobuyQty());
                    if (freeProductBO.getUomID() == productBO.getCaseUomId()) {
                        uomDes = freeProductBO.getUomDescription();
                    } else if (freeProductBO.getUomID() == productBO.getOuUomid()) {
                        uomDes = freeProductBO.getUomDescription();
                    } else if (freeProductBO.getUomID() == productBO.getPcUomid()) {
                        uomDes = freeProductBO.getUomDescription();
                    }

                }

            }


        }

        String freeQty;
        if (fromQty == toQty) {
            freeQty = fromQty + " " + uomDes;
        } else {
            freeQty = fromQty + "-" + toQty + " " + uomDes;
        }

        layout_child = new LinearLayout(mContext);
        layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        layout_child.setOrientation(LinearLayout.HORIZONTAL);

        productNameTV.setText(sb.toString());
        productNameTV.setWidth(mSlabWiseProductNameWidth);
        layout_child.addView(productNameTV);

        TextView slabTV = getDefaultTextView();
        slabTV.setText(freeQty);
        slabTV.setWidth(mSlabWiseSchemeNameWidth);

        layout_child.addView(slabTV);
        layout_parent.addView(layout_child);
        return layout_parent;
    }

    private LinearLayout addSlabWiseFreeProductONLYLogic(String schemeId, String groupName) {

        LinearLayout layout_parent = new LinearLayout(mContext);
        layout_parent.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout_parent.setBackgroundColor(Color.WHITE);
        } else {
            layout_parent.setBackgroundColor(Color.WHITE);
        }

        LinearLayout layout_child = new LinearLayout(mContext);
        layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        layout_child.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = getDefaultTextView();
        txt.setText("SKU");
        txt.setWidth(mSlabWiseProductNameWidth);

        layout_child.addView(txt);

        TextView schemeNameTV = getDefaultTextView();
        SchemeBO schemeBO = schemeHelper.getSchemeById().get(schemeId);
        schemeNameTV.setText(schemeBO.getScheme());
        schemeNameTV.setWidth(mSlabWiseSchemeNameWidth);

        layout_child.addView(schemeNameTV);
        layout_parent.addView(layout_child);

        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        for (SchemeProductBO freeProductBO : freeProductList) {

            if (freeProductBO.getGroupName().equals(groupName)) {
                ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(freeProductBO.getProductId());
                if (productBO != null) {

                    layout_child = new LinearLayout(mContext);
                    layout_child.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    layout_child.setOrientation(LinearLayout.HORIZONTAL);
                    TextView productNameTV = getDefaultTextView();
                    productNameTV.setWidth(mSlabWiseProductNameWidth);

                    layout_child.addView(productNameTV);

                    String freeQty;
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

                    TextView text_freeQuantity = getDefaultTextView();
                    text_freeQuantity.setText(freeQty);
                    text_freeQuantity.setWidth(mSlabWiseSchemeNameWidth);

                    layout_child.addView(text_freeQuantity);
                    layout_parent.addView(layout_child);
                }

            }
        }
        return layout_parent;
    }

   /* private boolean isChildSKUAvailable(String mProductID){
        for(ProductMasterBO pro)

    }*/
}
