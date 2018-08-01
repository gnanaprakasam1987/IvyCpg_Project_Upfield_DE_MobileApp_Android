package com.ivy.cpg.view.order.scheme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.platform.comapi.map.H;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.sd.png.view.CatalogOrder;
import com.ivy.sd.png.view.CrownReturnActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.InitiativeActivity;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.sd.png.view.RemarksDialog;

import org.apache.http.conn.scheme.Scheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This screen will show list of schemes applied for current order from that user can select/Reject/Modify
 */
public class QPSSchemeApply extends IvyBaseActivityNoActionBar {
    private static final String TAG = "Scheme Apply";

    private SchemeDetailsMasterHelper schemeHelper;
    private ExpandableListView mExpandableLV;
    private BusinessModel bModel;

    private boolean isClick;
    private EditText QUANTITY;
    private String append = "";
    private String screenCode = "MENU_STK_ORD";
    private SchemeExpandableAdapter mExpandableAdapterNew;
    private ArrayList<QPSListBO> mSchemeDoneList;
    private String fromOrderScreen = "";
    private String schemeViewTxt = "View";
    private SchemeFreeProductSelectionDialog mSchemeDialog;
    private InputMethodManager inputManager;
    int selectedChldPosition = 0, selectedParentPosition = 0;
    HashMap<Integer, SchemeBO> currentSchemeList;
    HashMap<Integer, SchemeBO> nextSchemeList;
    List<SchemeBO> schemeIDList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qps_apply_scheme);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null)
            setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.Scheme_apply));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);
        schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mExpandableLV = findViewById(R.id.elv);
        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setTypeface(bModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        findViewById(R.id.calcdot).setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
                fromOrderScreen = extras.getString("ForScheme", "STD_ORDER");
            }
        }

        try {
            if (bModel.labelsMasterHelper.applyLabels("scheme_view") != null)
                schemeViewTxt = bModel.labelsMasterHelper.applyLabels("scheme_view");
            else schemeViewTxt = getResources().getString(R.string.view);
        } catch (Exception e) {
            Commons.printException(e);
        }


        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> addedProductIDList = new ArrayList<>();
                for (QPSListBO qpsBO : mSchemeDoneList) {
                    Vector<SchemeProductBO> schemeProducts = qpsBO.getSchemeList();
                    for (SchemeProductBO schemeProduct : schemeProducts) {
                        for (ProductMasterBO product : bModel.productHelper.getProductMaster()) {
                            int totalQty = 0;
                            if (schemeProduct.getProductId().equals(product.getProductID()) && !addedProductIDList.contains(schemeProduct.getProductId())) {
                                totalQty = totalQty + schemeProduct.getIncreasedPcsQty();
                                totalQty = totalQty + (schemeProduct.getIncreasedCasesQty() * product.getCaseSize());
                                product.setIncreasedPcs(totalQty);
                                product.setOrderedPcsQty(product.getOrderedPcsQty() + totalQty);
                                addedProductIDList.add(schemeProduct.getProductId());
                            }
                        }
                    }
                }
//                if (isSchemeAplied(schemeHelper.getAppliedSchemeList())) {
//                    schemeHelper.clearOffInvoiceSchemeList();
                click(2);
//                } else {
//                    showAlert(getResources().getString(R.string.you_have_unchecked_applicable_scheme), 1);
//                }
            }
        });

        if (fromOrderScreen.equalsIgnoreCase("MENU_STK_ORD") ||
                fromOrderScreen.equalsIgnoreCase("MENU_ORDER") ||
                fromOrderScreen.equalsIgnoreCase("MENU_CATALOG_ORDER")) {
            new SchemeApplyAsync().execute();

        } else {
//            mSchemeDoneList = schemeHelper.getAppliedSchemeList();
//            if (mSchemeDoneList.size() > 0) {
//                mExpandableAdapterNew = new SchemeExpandableAdapter();
//                mExpandableLV.setAdapter(mExpandableAdapterNew);
//            }
        }


        if (!schemeHelper.IS_SCHEME_EDITABLE)
            ((LinearLayout) findViewById(R.id.footer)).setVisibility(View.GONE);

    }

    /**
     * @param mSchemeDoneList
     * @return false  - if scheme apply done in partially
     * @defalut flag is true
     */
    private boolean isSchemeAplied(ArrayList<SchemeBO> mSchemeDoneList) {
        boolean isFlag = true;
        if (mSchemeDoneList.size() > 0)
            for (SchemeBO schBo : mSchemeDoneList) {
                if (!schBo.isPriceTypeSeleted() && !schBo.isAmountTypeSelected()
                        && !schBo.isQuantityTypeSelected() && !schBo.isDiscountPrecentSelected())
                    isFlag = false;
            }

        return isFlag;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Getting scheme applied list by giving product master list
     */
    private class SchemeApplyAsync extends AsyncTask<Void, Void, Boolean> {
        HashMap<Integer, SchemeBO> parentSchemeList;


        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                parentSchemeList = new HashMap<>();
                currentSchemeList = new HashMap<>();
                nextSchemeList = new HashMap<>();

                mSchemeDoneList = new ArrayList<>();
                schemeHelper.resetSchemeQPSList();
                //For setting Ordered SKUs and check Slab in the object.
                schemeIDList = schemeHelper.getSchemeList();
                checkSlabandsetProduct(schemeIDList, currentSchemeList, nextSchemeList);
                mSchemeDoneList = buildListView(currentSchemeList, nextSchemeList);
                return true;
            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }


        }

        @Override
        protected void onPostExecute(Boolean isDone) {
            super.onPostExecute(isDone);
            if (isDone) {
                if (mSchemeDoneList.size() > 0) {
                    mExpandableAdapterNew = new SchemeExpandableAdapter();
                    mExpandableLV.setAdapter(mExpandableAdapterNew);
                }
            }
        }
    }

    private void checkSlabandsetProduct(List<SchemeBO> schemeIDList, HashMap<Integer, SchemeBO> currentSchemeList, HashMap<Integer, SchemeBO> nextSchemeList) {
        String groupName = "";
        HashMap<Integer, SchemeBO> tempList = new HashMap<>();
        for (SchemeBO schemeHeader : schemeIDList) {
            float totalPiecesQty = 0, totalPriceQty = 0;
            for (SchemeProductBO schemeProduct : schemeHeader.getBuyingProducts()) {
                schemeHeader.setFromQty(schemeProduct.getBuyQty());
                schemeHeader.setToQty(schemeProduct.getTobuyQty());
                schemeProduct.setParentID(String.valueOf(schemeHeader.getParentId()));
                schemeProduct.setGetType(schemeHeader.getGetType());
                for (ProductMasterBO product : bModel.productHelper.getProductMaster()) {
                    if (schemeProduct.getProductId().equalsIgnoreCase(product.getProductID())) {
                        if (product.getOrderedCaseQty() > 0) {
                            int qty = product.getOrderedCaseQty();
                            schemeProduct.setOrderedCasesQty(qty);
                            schemeProduct.setCasesPrice(product.getCsrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                //Converting to Pieces
                                totalPiecesQty = totalPiecesQty + (qty * product.getCaseSize());
                                totalPriceQty = totalPriceQty + (product.getSrp() * (qty * product.getCaseSize()));
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * (qty * product.getCaseSize()));
                            }
                        }
                        if (product.getOrderedPcsQty() > 0) {
                            int qty = product.getOrderedPcsQty();
                            schemeProduct.setOrderedPcsQty(qty);
                            schemeProduct.setPcsPrice(product.getSrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                totalPiecesQty = totalPiecesQty + qty;
                                totalPriceQty = totalPriceQty + (product.getSrp() * qty);
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * qty);
                            }
                        }
                        if (schemeProduct.getIncreasedCasesQty() > 0) {
                            schemeProduct.setCasesPrice(product.getCsrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                //Converting to Pieces
                                totalPiecesQty = totalPiecesQty + ((schemeProduct.getIncreasedCasesQty() != 0) ? (schemeProduct.getIncreasedCasesQty() * product.getCaseSize()) : 0);
                                totalPriceQty = totalPriceQty + (product.getSrp() * ((schemeProduct.getIncreasedCasesQty() != 0) ? (schemeProduct.getIncreasedCasesQty() * product.getCaseSize()) : 0));
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * ((schemeProduct.getIncreasedCasesQty() != 0) ? (schemeProduct.getIncreasedCasesQty() * product.getCaseSize()) : 0));
                            }
                        } else if (schemeProduct.getIncreasedPcsQty() > 0) {
                            schemeProduct.setPcsPrice(product.getSrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                totalPiecesQty = totalPiecesQty + ((schemeProduct.getIncreasedPcsQty() != 0) ? schemeProduct.getIncreasedPcsQty() : 0);
                                totalPriceQty = totalPriceQty + (product.getSrp() * ((schemeProduct.getIncreasedPcsQty() != 0) ? schemeProduct.getIncreasedPcsQty() : 0));
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * ((schemeProduct.getIncreasedPcsQty() != 0) ? schemeProduct.getIncreasedPcsQty() : 0));
                            }
                        }
                        schemeHeader.setEveryCaseUOM(product.getCaseUomId() == schemeHeader.getEveryUomId());
                    }
                }
            }
            if ((totalPiecesQty >= schemeHeader.getFromQty()) && (totalPiecesQty <= schemeHeader.getToQty())) {
                schemeHeader.setTotalOrderQty(totalPiecesQty);
                schemeHeader.setTotalPriceQty(totalPriceQty);
                currentSchemeList.put(schemeHeader.getParentId(), schemeHeader);
            }

            int currentCount = 0;
            for (Map.Entry<Integer, SchemeBO> entry : currentSchemeList.entrySet()) {
                if (entry.getKey().equals(schemeHeader.getParentId())) {
                    currentCount++;
                }
            }
            int nextCount = 0;
            for (Map.Entry<Integer, SchemeBO> entry : nextSchemeList.entrySet()) {
                if (entry.getKey().equals(schemeHeader.getParentId())) {
                    nextCount++;
                }
            }

            if (groupName.equals(schemeHeader.getGroupName()) && currentCount == 1 && nextCount == 0
                    && !currentSchemeList.equals(nextSchemeList)) {
                HashMap<Integer, SchemeBO> tempNextList = new HashMap<>();
                tempNextList.put(schemeHeader.getParentId(), schemeHeader);
                if (!tempNextList.equals(currentSchemeList))
                    nextSchemeList.put(schemeHeader.getParentId(), schemeHeader);
            }
            groupName = schemeHeader.getGroupName();
            if ((totalPiecesQty >= schemeHeader.getFromQty()) && (totalPiecesQty >= schemeHeader.getToQty())) {
                schemeHeader.setTotalOrderQty(totalPiecesQty);
                tempList.put(schemeHeader.getParentId(), schemeHeader);
            }
        }

        boolean isKeyAlreadyAdded = false;
        for (Map.Entry<Integer, SchemeBO> entry : tempList.entrySet()) {
            if (currentSchemeList.containsKey(entry.getKey())) {
                isKeyAlreadyAdded = true;
            }
        }

        if (!isKeyAlreadyAdded) {
            currentSchemeList.putAll(tempList);
        }
    }

    private ArrayList<QPSListBO> buildListView(HashMap<Integer, SchemeBO> currentSchemeList, HashMap<Integer, SchemeBO> nextSchemeList) {
        mSchemeDoneList.clear();
        HashMap<String, ArrayList<ProductMasterBO>> historyMap = schemeHelper.getSchemeHistoryListBySchemeId();
        List<ParentSchemeBO> parentSchemeList = schemeHelper.getParentSchemeList();
        for (ParentSchemeBO parentSchemeBO : parentSchemeList) {
            Vector<SchemeProductBO> schemeList = new Vector<>();

            for (Map.Entry<Integer, SchemeBO> entry : currentSchemeList.entrySet()) {
                if (entry.getKey() == parentSchemeBO.getSchemeID()) {
                    SchemeBO schemeHeader = entry.getValue();
                    if (historyMap.get(schemeHeader.getSchemeId()) != null && historyMap.get(schemeHeader.getSchemeId()).size() > 0) {
                        //Calculating Header Item with Ach History
                        String type = "", buyType = "";
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            type = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                        }

                        //Calculating Static cumulative purchase, Cur Sch Details
                        double cumulativePurchase = 0, totalAmt = 0;
                        for (ProductMasterBO product : historyMap.get(schemeHeader.getSchemeId())) {
                            totalAmt = totalAmt + (product.getOrderedPcsQty() * getPcsPrice(product.getProductID()));
                            if (buyType.equals("SV")) {
                                cumulativePurchase = cumulativePurchase + product.getTotalamount();
                            } else {
                                cumulativePurchase = cumulativePurchase + product.getOrderedPcsQty();
                            }
                        }
                        parentSchemeBO.setCumulativePurchase(cumulativePurchase);

                        List<SchemeProductBO> schemeBOList = checkSlab(entry.getKey(), parentSchemeBO.getCumulativePurchase());
                        for (SchemeProductBO schemeBO : schemeBOList) {
                            if (schemeBO != null) {
                                if (type.equals("PER")) {
                                    if (buyType.equals("SV")) {
                                        parentSchemeBO.setCurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * parentSchemeBO.getCumulativePurchase());
                                    } else {
                                        parentSchemeBO.setCurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * totalAmt);
                                    }
                                    parentSchemeBO.setCurSlabrsorPer(schemeBO.getMinPercent());
                                } else if (type.equals("VALUE")) {
                                    double cumulativePurchaseValue = 0;
                                    if (schemeHeader.getEveryQty() > 0 && schemeHeader.isEveryCaseUOM()) {
                                        cumulativePurchaseValue = (parentSchemeBO.getCumulativePurchase() / (schemeHeader.getEveryQty()));
                                    } else if (schemeHeader.getEveryQty() > 0 && !schemeHeader.isEveryCaseUOM()) {
                                        cumulativePurchaseValue = Math.floor(parentSchemeBO.getCumulativePurchase() / schemeHeader.getEveryQty());
                                        cumulativePurchaseValue = cumulativePurchaseValue * schemeBO.getMinAmount();
                                        parentSchemeBO.setCurSlabCumSchAmt(cumulativePurchaseValue);
                                    } else {
                                        parentSchemeBO.setCurSlabCumSchAmt(schemeBO.getMinAmount());
                                    }
                                    parentSchemeBO.setCurSlabrsorPer(schemeBO.getMinAmount());
                                }
                            }
                        }

                        //Calculating Dynamic cumulative purchase, Cur Sch Details
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            if (buyType.matches("SV|QTY")) {
                                parentSchemeBO.setCalculatedCumulativePurchase(parentSchemeBO.getCumulativePurchase() + (schemeHeader.getTotalOrderQty()));
                            }
                            schemeList.add(schemeBO);
                        }

                        List<SchemeProductBO> calculatedSchemeBOList = checkSlab(entry.getKey(), parentSchemeBO.getCalculatedCumulativePurchase());
                        for (SchemeProductBO schemeBO : calculatedSchemeBOList) {
                            if (type.equals("PER")) {
                                if (buyType.equals("SV")) {
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * parentSchemeBO.getCalculatedCumulativePurchase());
                                } else {
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * (totalAmt + schemeHeader.getTotalPriceQty()));
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinPercent());
                            } else if (type.equals("VALUE")) {
                                double cumulativePurchaseValue = 0;
                                if (schemeHeader.getEveryQty() > 0 && schemeHeader.isEveryCaseUOM()) {
                                    cumulativePurchaseValue = (parentSchemeBO.getCumulativePurchase() / (schemeHeader.getEveryQty()));
                                } else if (schemeHeader.getEveryQty() > 0 && !schemeHeader.isEveryCaseUOM()) {
                                    cumulativePurchaseValue = Math.floor(parentSchemeBO.getCalculatedCumulativePurchase() / schemeHeader.getEveryQty());
                                    cumulativePurchaseValue = cumulativePurchaseValue * schemeBO.getMinAmount();
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt(cumulativePurchaseValue);
                                } else {
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt(schemeBO.getMinAmount());
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinAmount());
                            }
                        }
                    } else {
                        //Calculating Header Item without Ach History
                        parentSchemeBO.setCumulativePurchase(0);
                        parentSchemeBO.setCurSlabCumSchAmt(0);
                        parentSchemeBO.setCurSlabrsorPer(0);

                        String type = "", buyType = "";
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            type = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            if (buyType.matches("SV|QTY")) {
                                parentSchemeBO.setCalculatedCumulativePurchase((schemeHeader.getTotalOrderQty()));
                            }
                            schemeList.add(schemeBO);
                        }
                        for (SchemeProductBO schemeBO : schemeHeader.getFreeProducts()) {
                            if (type.equals("PER")) {
                                if (buyType.equals("SV")) {
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * parentSchemeBO.getCalculatedCumulativePurchase());
                                } else {
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * schemeHeader.getTotalPriceQty());
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinPercent());
                            } else if (type.equals("VALUE")) {
                                double cumulativePurchaseValue = 0;
                                if (schemeHeader.getEveryQty() > 0 && schemeHeader.isEveryCaseUOM()) {
                                    cumulativePurchaseValue = (parentSchemeBO.getCumulativePurchase() / (schemeHeader.getEveryQty()));
                                } else if (schemeHeader.getEveryQty() > 0 && !schemeHeader.isEveryCaseUOM()) {
                                    cumulativePurchaseValue = Math.floor(parentSchemeBO.getCalculatedCumulativePurchase() / schemeHeader.getEveryQty());
                                    cumulativePurchaseValue = cumulativePurchaseValue * schemeBO.getMinAmount();
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt(cumulativePurchaseValue);
                                } else {
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt(schemeBO.getMinAmount());
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinAmount());
                            }
                        }
                    }
                }
            }

            boolean isNextSchemeAvailable = false;
            for (Map.Entry<Integer, SchemeBO> entry : nextSchemeList.entrySet()) {
                double schemebuyQty = 0;
                if (entry.getKey() == parentSchemeBO.getSchemeID()) {
                    isNextSchemeAvailable = true;
                    SchemeBO schemeHeader = entry.getValue();
                    if (historyMap.get(schemeHeader.getSchemeId()) != null && historyMap.get(schemeHeader.getSchemeId()).size() > 0) {
                        //Calculating Next Slab Item with Ach History
                        String type = "", buyType = "";
                        List<SchemeProductBO> schemeBuyingList = checkNextSlabBuyProducts(entry.getKey(), parentSchemeBO.getCumulativePurchase());
                        for (SchemeProductBO schemeBO : schemeBuyingList) {
                            type = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            schemebuyQty = schemeBO.getBuyQty();
                            parentSchemeBO.setNextSlabBalance(Math.max(0, schemeBO.getBuyQty() - parentSchemeBO.getCumulativePurchase()));
                        }

                        List<SchemeProductBO> schemeBOList = checkSlab(entry.getKey(), (parentSchemeBO.getNextSlabBalance() + parentSchemeBO.getCumulativePurchase()));
                        for (SchemeProductBO schemeBO : schemeBOList) {
                            if (schemeBO != null) {
                                if (type.equals("PER")) {
                                    parentSchemeBO.setNextSlabCumSchAmt((buyType.equals("SV")) ? ((schemeBO.getMinPercent() / 100) * schemebuyQty) : 0);
                                    parentSchemeBO.setNextSlabrsorPer(schemeBO.getMinPercent());
                                } else if (type.equals("VALUE")) {
                                    parentSchemeBO.setNextSlabCumSchAmt((buyType.equals("SV")) ? schemeBO.getMinAmount() : 0);
                                    parentSchemeBO.setNextSlabrsorPer(schemeBO.getMinAmount());
                                }
                            }
                        }

                        schemebuyQty = 0;
                        String getType = "";
                        List<SchemeProductBO> schemenextBuyingList = checkNextSlabBuyProducts(entry.getKey(), parentSchemeBO.getCalculatedCumulativePurchase());
                        for (SchemeProductBO schemeBO : schemenextBuyingList) {
                            getType = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            //if (schemeBO.getBuyType() != null && schemeBO.getBuyType().matches("SV|QTY")) {
                            schemebuyQty = schemeBO.getBuyQty();
                            parentSchemeBO.setCalculatednextSlabBalance(Math.max(0, schemeBO.getBuyQty() - parentSchemeBO.getCalculatedCumulativePurchase()));
                            //}
                        }

                        if (parentSchemeBO.getCalculatednextSlabBalance() > 0) {
                            List<SchemeProductBO> nextSchemeBOList = checkNextSlabFreeProducts(entry.getKey(), schemebuyQty);
                            for (SchemeProductBO schemeBO : nextSchemeBOList) {
                                if (getType.equals("PER")) {
                                    parentSchemeBO.setCalculatednextSlabCumSchAmt((buyType.equals("SV")) ? ((schemeBO.getMinPercent() / 100) * schemebuyQty) : 0);
                                    parentSchemeBO.setCalculatednextSlabrsorPer(schemeBO.getMinPercent());
                                } else if (getType.equals("VALUE")) {
                                    parentSchemeBO.setCalculatednextSlabCumSchAmt((buyType.equals("SV")) ? (parentSchemeBO.getCalculatednextSlabBalance() > 0 ? schemeBO.getMinAmount() : 0) : 0);
                                    parentSchemeBO.setCalculatednextSlabrsorPer(parentSchemeBO.getCalculatednextSlabBalance() > 0 ? schemeBO.getMinAmount() : 0);
                                }
                            }
                        } else {
                            parentSchemeBO.setCalculatednextSlabCumSchAmt(0);
                            parentSchemeBO.setCalculatednextSlabrsorPer(0);
                        }
                    } else {
                        //Calculating Next Slab Item without Ach History
                        parentSchemeBO.setNextSlabBalance(0);
                        parentSchemeBO.setNextSlabCumSchAmt(0);
                        parentSchemeBO.setNextSlabrsorPer(0);

                        String getType = "", buyType = "";
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            getType = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            if (schemeBO.getBuyType().matches("SV|QTY")) {
                                schemebuyQty = schemeBO.getBuyQty();
                                parentSchemeBO.setCalculatednextSlabBalance(Math.max(0, schemeBO.getBuyQty() - parentSchemeBO.getCalculatedCumulativePurchase()));
                            }
                        }
                        for (SchemeProductBO schemeBO : schemeHeader.getFreeProducts()) {
                            if (getType.equals("PER")) {
                                parentSchemeBO.setCalculatednextSlabCumSchAmt((buyType.equals("SV")) ? (schemeBO.getMinPercent() / 100) * schemebuyQty : 0);
                                parentSchemeBO.setCalculatednextSlabrsorPer(schemeBO.getMinPercent());
                            } else if (getType.equals("VALUE")) {
                                parentSchemeBO.setCalculatednextSlabCumSchAmt((buyType.equals("SV")) ? (parentSchemeBO.getCalculatednextSlabBalance() > 0 ? schemeBO.getMinAmount() : 0) : 0);
                                parentSchemeBO.setCalculatednextSlabrsorPer(parentSchemeBO.getCalculatednextSlabBalance() > 0 ? schemeBO.getMinAmount() : 0);
                            }
                        }
                    }
                }
            }
            if (!isNextSchemeAvailable) {
                parentSchemeBO.setCalculatednextSlabBalance(0);
                parentSchemeBO.setCalculatednextSlabCumSchAmt(0);
                parentSchemeBO.setCalculatednextSlabrsorPer(0);
            }

            QPSListBO qpsListBO = new QPSListBO();
            qpsListBO.setParentScheme(parentSchemeBO);
            qpsListBO.setSchemeList(schemeList);
            mSchemeDoneList.add(qpsListBO);
        }
        return mSchemeDoneList;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_only_next, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_next).setVisible(false);
        if (screenCode.equalsIgnoreCase("CSale")) {
            menu.findItem(R.id.menu_counter_remark).setVisible(true);
        }
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            click(1);
            return true;
        } else if (i == R.id.menu_counter_remark) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            RemarksDialog dialog = new RemarksDialog("MENU_COUNTER");
            dialog.setCancelable(false);
            dialog.show(ft, "counter_scheme_remark");
        }
        return super.onOptionsItemSelected(item);
    }

    private void click(int action) {
        if (!isClick) {

            if (action == 1) {
                isClick = true;
                if ((bModel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bModel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                        && bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    Intent intent = new Intent(QPSSchemeApply.this,
                            CrownReturnActivity.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    Intent intent = new Intent(QPSSchemeApply.this,
                            BatchAllocation.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else {
                    Intent intent;
                    if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                        intent = new Intent(QPSSchemeApply.this, CatalogOrder.class);
                    } else {
                        intent = new Intent(QPSSchemeApply.this, StockAndOrder.class);
                    }
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);

                }
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();

            } else if (action == 2) {
                Intent i = new Intent(QPSSchemeApply.this, OrderSummary.class);
                i.putExtra("ScreenCode", screenCode);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_CANCELED) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                } else if (resultCode == RESULT_OK) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
        }
    }

    NextSlabSchemeDialog mSchemePromDialog;

    class SchemeExpandableAdapter extends BaseExpandableListAdapter {

        LayoutInflater mInflater;


        public SchemeExpandableAdapter() {
            mInflater = getLayoutInflater();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
            final SchemeProductDetailHolder holder;
            if (view == null) {
                holder = new SchemeProductDetailHolder();
                // Other views
                view = mInflater.inflate(R.layout.row_qps_scheme_slab_detail, parent, false);

                holder.tv_productName = view.findViewById(R.id.tv_productName);
                holder.tv_pcs_ordered_qty = view.findViewById(R.id.tv_pcs_ordered_qty);
                holder.tv_cases_ordered_qty = view.findViewById(R.id.tv_cases_ordered_qty);
                holder.tv_uom = view.findViewById(R.id.tv_uom);
                holder.tv_pcs_final_qty = view.findViewById(R.id.tv_pcs_final_qty);
                holder.tv_cases_final_qty = view.findViewById(R.id.tv_cases_final_qty);
                holder.tv_gqt = view.findViewById(R.id.tv_gqt);
                holder.lnrSchemeHeader = view.findViewById(R.id.lnrSchemeHeader);

                //typeface
                holder.tv_productName.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_pcs_ordered_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cases_ordered_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_uom.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_pcs_final_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cases_final_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_gqt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

//                holder.tv_pcs_final_qty.setOnTouchListener(new OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View view, MotionEvent motionEvent) {
//                        QUANTITY = holder.tv_pcs_final_qty;
//                        QUANTITY.setTag(holder.schemeProducts);
//                        selectedParentPosition = groupPosition;
//                        selectedChldPosition = childPosition;
//                        int inType = holder.tv_pcs_final_qty.getInputType();
//                        holder.tv_pcs_final_qty.setInputType(InputType.TYPE_NULL);
//                        holder.tv_pcs_final_qty.onTouchEvent(motionEvent);
//                        holder.tv_pcs_final_qty.setInputType(inType);
//                        holder.tv_pcs_final_qty.selectAll();
//                        holder.tv_pcs_final_qty.requestFocus();
//                        inputManager.hideSoftInputFromWindow(holder.tv_pcs_final_qty.getWindowToken(), 0);
//                        return true;
//                    }
//                });
                holder.tv_pcs_final_qty.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        QUANTITY = holder.tv_pcs_final_qty;
                        QUANTITY.setTag(holder.schemeProducts);
                        selectedParentPosition = groupPosition;
                        selectedChldPosition = childPosition;
                        int inType = holder.tv_pcs_final_qty.getInputType();
                        holder.tv_pcs_final_qty.setInputType(InputType.TYPE_NULL);
                        //holder.tv_pcs_final_qty.onTouchEvent(motionEvent);
                        holder.tv_pcs_final_qty.setInputType(inType);
                        holder.tv_pcs_final_qty.selectAll();
                        holder.tv_pcs_final_qty.requestFocus();
                        inputManager.hideSoftInputFromWindow(holder.tv_pcs_final_qty.getWindowToken(), 0);
                        //return true;
                    }
                });
                holder.tv_pcs_final_qty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        if (groupPosition == holder.schemeProducts.getParentPosition() && childPosition == holder.schemeProducts.getChildPosition()) {
//                        //if (SDUtil.convertToInt(s.toString()) > 0) {
                        schemeHelper.resetSchemeQPSList();
                        holder.schemeList = schemeHelper.getSchemeList();
                        for (SchemeBO scheme : holder.schemeList) {
                            List<SchemeProductBO> productList = scheme.getBuyingProducts();
                            for (SchemeProductBO product : productList) {
                                if (product.getProductId().equals(holder.schemeProducts.getProductId())) {
                                    product.setIncreasedPcsQty(SDUtil.convertToInt(s.toString()));
                                    product.setParentPosition(selectedParentPosition);
                                    product.setChildPosition(selectedChldPosition);
                                }
                            }
                        }
                        holder.currentSchemeList = new HashMap<>();
                        holder.nextSchemeList = new HashMap<>();
                        checkSlabandsetProduct(holder.schemeList, holder.currentSchemeList, holder.nextSchemeList);
                        buildListView(holder.currentSchemeList, holder.nextSchemeList);
                        mExpandableAdapterNew.notifyDataSetChanged();
//                        //}
//                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
//                holder.tv_cases_final_qty.setOnTouchListener(new OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View view, MotionEvent motionEvent) {
//                        QUANTITY = holder.tv_cases_final_qty;
//                        QUANTITY.setTag(holder.schemeProducts);
//                        selectedParentPosition = groupPosition;
//                        selectedChldPosition = childPosition;
//                        int inType = holder.tv_cases_final_qty.getInputType();
//                        holder.tv_cases_final_qty.setInputType(InputType.TYPE_NULL);
//                        //holder.tv_pcs_final_qty.onTouchEvent(motionEvent);
//                        holder.tv_cases_final_qty.setInputType(inType);
//                        holder.tv_cases_final_qty.selectAll();
//                        holder.tv_cases_final_qty.requestFocus();
//                        inputManager.hideSoftInputFromWindow(holder.tv_cases_final_qty.getWindowToken(), 0);
//                        return true;
//                    }
//                });
                holder.tv_cases_final_qty.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        QUANTITY = holder.tv_cases_final_qty;
                        QUANTITY.setTag(holder.schemeProducts);
                        selectedParentPosition = groupPosition;
                        selectedChldPosition = childPosition;
                        int inType = holder.tv_cases_final_qty.getInputType();
                        holder.tv_cases_final_qty.setInputType(InputType.TYPE_NULL);
                        //holder.tv_pcs_final_qty.onTouchEvent(motionEvent);
                        holder.tv_cases_final_qty.setInputType(inType);
                        holder.tv_cases_final_qty.selectAll();
                        holder.tv_cases_final_qty.requestFocus();
                        inputManager.hideSoftInputFromWindow(holder.tv_cases_final_qty.getWindowToken(), 0);
                        //return true;
                    }
                });
                holder.tv_cases_final_qty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //if (groupPosition == selectedParentPosition && childPosition == selectedChldPosition) {
                        //if (SDUtil.convertToInt(s.toString()) > 0) {
                        System.out.println("CUrrent Position Child " + groupPosition + "," + childPosition);
                        schemeHelper.resetSchemeQPSList();
                        holder.schemeList = schemeHelper.getSchemeList();
                        for (SchemeBO scheme : holder.schemeList) {
                            List<SchemeProductBO> productList = scheme.getBuyingProducts();
                            for (SchemeProductBO product : productList) {
                                if (product.getProductId().equals(holder.schemeProducts.getProductId())) {
                                    product.setIncreasedCasesQty(SDUtil.convertToInt(s.toString()));
                                    product.setParentPosition(selectedParentPosition);
                                    product.setChildPosition(selectedChldPosition);
                                }
                            }
                        }
                        holder.currentSchemeList = new HashMap<>();
                        holder.nextSchemeList = new HashMap<>();
                        checkSlabandsetProduct(holder.schemeList, holder.currentSchemeList, holder.nextSchemeList);
                        buildListView(holder.currentSchemeList, holder.nextSchemeList);
                        mExpandableAdapterNew.notifyDataSetChanged();
                        //}
                        //}
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                view.setTag(holder);
            } else {
                holder = (SchemeProductDetailHolder) view.getTag();
            }

            if (childPosition != 0) {
                holder.lnrSchemeHeader.setVisibility(View.GONE);
            } else {
                holder.lnrSchemeHeader.setVisibility(View.VISIBLE);
            }

            holder.schemeProducts = (SchemeProductBO) getChild(groupPosition, childPosition);
            holder.tv_productName.setText(holder.schemeProducts.getProductName().length() == 0 ?
                    holder.schemeProducts.getProductFullName() : holder.schemeProducts.getProductName());
            holder.tv_uom.setText(holder.schemeProducts.getUomDescription());
            holder.tv_pcs_ordered_qty.setText(holder.schemeProducts.getOrderedPcsQty() + "");
            holder.tv_cases_ordered_qty.setText(holder.schemeProducts.getOrderedCasesQty() + "");
            holder.tv_pcs_final_qty.setText(holder.schemeProducts.getIncreasedPcsQty() + "");
            holder.tv_cases_final_qty.setText(holder.schemeProducts.getIncreasedCasesQty() + "");
            holder.tv_gqt.setText((((holder.schemeProducts.getOrderedPcsQty() +
                    holder.schemeProducts.getIncreasedPcsQty()) * holder.schemeProducts.getPcsPrice()) + ((holder.schemeProducts.getOrderedCasesQty() +
                    holder.schemeProducts.getIncreasedCasesQty()) * holder.schemeProducts.getCasesPrice())) + "");


            if (holder.schemeProducts.getParentPosition() == groupPosition && holder.schemeProducts.getChildPosition() == childPosition) {
                holder.tv_pcs_final_qty.setFocusable(true);
                holder.tv_pcs_final_qty.requestFocus();
            }
            return view;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            List<SchemeProductBO> productList = mSchemeDoneList.get(groupPosition).getSchemeList();
            return productList.get(childPosition);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mSchemeDoneList.get(groupPosition).getSchemeList().size();

        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public int getGroupCount() {
            if (mSchemeDoneList == null)
                return 0;
            return mSchemeDoneList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View view, ViewGroup parent) {

            final SchemeProductHolder holder;

            if (view == null) {
                holder = new SchemeProductHolder();
                view = mInflater.inflate(R.layout.row_qps_scheme, parent,
                        false);

                holder.tv_scheme = view
                        .findViewById(R.id.tv_scheme);
                holder.tv_schemeDuration = view
                        .findViewById(R.id.tv_schemeduration);
                holder.tv_schemeType = view.findViewById(R.id.tv_schemetype);
                holder.tv_cumulative_purchase = view
                        .findViewById(R.id.tv_cumulative_purchase);
                holder.tv_curslab_sch_amt = view
                        .findViewById(R.id.tv_curslab_sch_amt);
                holder.tv_curslab_rs_per = view
                        .findViewById(R.id.tv_curslab_rs_per);
                holder.tv_nextslab = view
                        .findViewById(R.id.tv_nextslab);
                holder.tv_nextslab_sch_amt = view
                        .findViewById(R.id.tv_nextslab_sch_amt);
                holder.tv_nextslab_rs_per = view
                        .findViewById(R.id.tv_nextslab_rs_per);
                holder.tv_calculated_cumulative_purchase = view
                        .findViewById(R.id.tv_current_cumulative_purchase);
                holder.tv_calculated_curslab_sch_amt = view
                        .findViewById(R.id.tv_current_cumulative_scheme_discount);
                holder.tv_calculated_curslab_rs_per = view
                        .findViewById(R.id.tv_current_scheme_per_amt);
                holder.tv_calculated_nextslab = view
                        .findViewById(R.id.tv_current_balance_to_nextslab);
                holder.tv_calculated_nextslab_sch_amt = view
                        .findViewById(R.id.tv_next_cumulative_scheme_discount);
                holder.tv_calculated_nextslab_rs_per = view
                        .findViewById(R.id.tv_next_scheme_per_amt);
                //typeface
                holder.tv_scheme.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_schemeDuration.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_schemeType.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cumulative_purchase.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_curslab_sch_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_curslab_rs_per.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_nextslab.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_nextslab_sch_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_nextslab_rs_per.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.tv_calculated_cumulative_purchase.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_calculated_curslab_sch_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_calculated_curslab_rs_per.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_calculated_nextslab.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_calculated_nextslab_sch_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_calculated_nextslab_rs_per.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

//                holder.upArrow.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//
//                        schemeHelper.loadSchemePromotion(getApplicationContext(),
//                                holder.schemeBO.getSchemeId(),
//                                holder.schemeBO.getParentLogic(),
//                                holder.schemeBO.getChannelId(),
//                                holder.schemeBO.getSubChannelId(),
//                                holder.productBO.getProductID(),
//                                holder.schemeBO.getQuantity());
//                        if (schemeHelper.getSchemePromotion() != null
//                                && schemeHelper.getSchemePromotion()
//                                .size() > 0) {
//                            if (mSchemePromDialog == null) {
////                                mSchemePromDialog = new NextSlabSchemeDialog(
////                                        QPSSchemeApply.this, false, null,
////                                        QPSSchemeApply.this);
//
//                                mSchemePromDialog.setCancelable(false);
//                            }
//                            mSchemePromDialog.show();
//                        } else {
//                            Toast.makeText(QPSSchemeApply.this,
//                                    "No Better Promotional Scheme available",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                });

                view.setTag(holder);
            } else {
                holder = (SchemeProductHolder) view.getTag();
            }

            holder.parentSchemeBO = mSchemeDoneList.get(groupPosition).getParentScheme();
            holder.tv_scheme.setText(holder.parentSchemeBO.getSchemeDesc());
            String periodStart = "";
            String periodEnd = "";
            holder.tv_schemeDuration.setText(periodStart + " - " + periodEnd);
            holder.tv_schemeType.setText(holder.parentSchemeBO.getBuyType());

            holder.tv_cumulative_purchase.setText(SDUtil.format(holder.parentSchemeBO.getCumulativePurchase(), 2, 0) + "");
            holder.tv_curslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getCurSlabCumSchAmt(), 2, 0) + "");
            holder.tv_curslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getCurSlabrsorPer(), 2, 0) + "");
            holder.tv_nextslab.setText(SDUtil.format(holder.parentSchemeBO.getNextSlabBalance(), 2, 0) + "");
            holder.tv_nextslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getNextSlabCumSchAmt(), 2, 0) + "");
            holder.tv_nextslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getNextSlabrsorPer(), 2, 0) + "");

            holder.tv_calculated_cumulative_purchase.setText(SDUtil.format(holder.parentSchemeBO.getCalculatedCumulativePurchase(), 2, 0) + "");
            holder.tv_calculated_curslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getCalculatedcurSlabCumSchAmt(), 2, 0) + "");
            holder.tv_calculated_curslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getCalculatedcurSlabrsorPer(), 2, 0) + "");
            holder.tv_calculated_nextslab.setText(SDUtil.format(holder.parentSchemeBO.getCalculatednextSlabBalance(), 2, 0) + "");
            holder.tv_calculated_nextslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getCalculatednextSlabCumSchAmt(), 2, 0) + "");
            holder.tv_calculated_nextslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getCalculatednextSlabrsorPer(), 2, 0) + "");
            return view;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


    }

    private class SchemeProductHolder {
        private ProductMasterBO productBO;
        private ParentSchemeBO parentSchemeBO;
        private TextView tv_scheme;
        private TextView tv_schemeDuration;
        private TextView tv_schemeType;
        private TextView tv_cumulative_purchase;
        private TextView tv_curslab_sch_amt;
        private TextView tv_curslab_rs_per;
        private TextView tv_nextslab;
        private TextView tv_nextslab_sch_amt;
        private TextView tv_nextslab_rs_per;

        private TextView tv_calculated_cumulative_purchase;
        private TextView tv_calculated_curslab_sch_amt;
        private TextView tv_calculated_curslab_rs_per;
        private TextView tv_calculated_nextslab;
        private TextView tv_calculated_nextslab_sch_amt;
        private TextView tv_calculated_nextslab_rs_per;


    }

    static class SchemeProductDetailHolder {
        SchemeProductBO schemeProducts;
        TextView tv_productName, tv_pcs_ordered_qty, tv_cases_ordered_qty,
                tv_uom, tv_gqt;
        EditText tv_pcs_final_qty, tv_cases_final_qty;
        LinearLayout lnrSchemeHeader;

        HashMap<Integer, SchemeBO> currentSchemeList = new HashMap<>();
        HashMap<Integer, SchemeBO> nextSchemeList = new HashMap<>();
        List<SchemeBO> schemeList = new ArrayList<>();
    }

    /**
     * Show alert dialog
     *
     * @param message Message to show in dialog
     */
    private void showAlert(String message, int id) {

        switch (id) {
            case 0:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getResources().getString(R.string.scheme_apply));
                dialog.setMessage(message);
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

                bModel.applyAlertDialogTheme(dialog);
                break;

            case 1:
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
                dialog1.setTitle(getResources().getString(R.string.Scheme_apply));
                dialog1.setMessage(message);
                dialog1.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        schemeHelper.clearOffInvoiceSchemeList();
                        click(2);
                        dialog.dismiss();
                    }
                });
                dialog1.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                bModel.applyAlertDialogTheme(dialog1);
                break;

        }
    }

    public void eff() {

        String s = QUANTITY.getText().toString();
        int maxLength = 5;

        if (QUANTITY.getText().toString().contains(".")) {
            maxLength = 8;

        }

        if (s.length() < maxLength) {
            if (!s.equals("0") && !s.equals("0.0"))
                QUANTITY.setText(QUANTITY.getText() + append);
            else
                QUANTITY.setText(append);
        }

    }

    public void numberPressed(View vw) {

        if (QUANTITY == null) {

            showAlert(getResources().getString(R.string.please_select_item), 0);

        } else {
            int id = vw.getId();
            if (id == R.id.calcone) {
                append = "1";
                eff();
            } else if (id == R.id.calctwo) {
                append = "2";
                eff();
            } else if (id == R.id.calcthree) {
                append = "3";
                eff();
            } else if (id == R.id.calcfour) {
                append = "4";
                eff();
            } else if (id == R.id.calcfive) {
                append = "5";
                eff();
            } else if (id == R.id.calcsix) {
                append = "6";
                eff();
            } else if (id == R.id.calcseven) {
                append = "7";
                eff();
            } else if (id == R.id.calceight) {
                append = "8";
                eff();
            } else if (id == R.id.calcnine) {
                append = "9";
                eff();
            } else if (id == R.id.calczero) {
                append = "0";
                eff();
            } else if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "0";
                    }
                }
                QUANTITY.setText(s);

            } else if (id == R.id.calcdot) {


                if (!QUANTITY.getText().toString().contains(".")) {
                    append = ".";
                    eff();

                } else {
                    append = "";
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private List<SchemeProductBO> checkSlab(int schemeID, double qty) {
        List<SchemeBO> availableList = new ArrayList<>();
        List<SchemeProductBO> productList = new ArrayList<>();
        boolean isAvailable = false, upFlag = false;
        int i = 0;
        for (SchemeBO schemeHeader : schemeIDList) {
            if (schemeHeader.getParentId() == schemeID) {
                availableList.add(schemeHeader);
            }
        }
        for (SchemeBO schemeBO : availableList) {
            if (qty >= schemeBO.getFromQty() && qty <= schemeBO.getToQty()) {
                isAvailable = true;
                productList.addAll(schemeBO.getFreeProducts());
            }
            if (qty >= schemeBO.getFromQty() && qty >= schemeBO.getToQty()) {
                upFlag = true;
            }
            i++;
        }
        if (!isAvailable && upFlag) productList.addAll(availableList.get(i - 1).getFreeProducts());
        return productList;
    }

    private List<SchemeProductBO> checkNextSlabBuyProducts(int schemeID, double qty) {
        List<SchemeBO> availableList = new ArrayList<>();

        List<SchemeProductBO> currentList = new ArrayList<>();
        List<SchemeProductBO> nextList = new ArrayList<>();
        if (qty > 0) {
            boolean isAvailable = false, upFlag = false, downFlag = false;
            int i = 0;
            for (SchemeBO schemeHeader : schemeIDList) {
                if (schemeHeader.getParentId() == schemeID) {
                    availableList.add(schemeHeader);
                }
            }

            for (SchemeBO schemeBO : availableList) {
                if (currentList.size() > 0 && nextList.size() == 0) {
                    isAvailable = true;
                    nextList.addAll(schemeBO.getBuyingProducts());
                }
                if (qty >= schemeBO.getFromQty() && qty <= schemeBO.getToQty()) {
                    currentList.addAll(schemeBO.getBuyingProducts());
                }
                if (qty >= schemeBO.getFromQty() && qty >= schemeBO.getToQty() && currentList.size() == 0) {
                    upFlag = true;
                }
                if (qty <= schemeBO.getFromQty() && qty <= schemeBO.getToQty() && currentList.size() == 0) {
                    downFlag = true;
                }
                i++;
            }


            if (!isAvailable && upFlag)
                nextList.addAll(availableList.get(i - 1).getBuyingProducts());
            if (!isAvailable && downFlag) nextList.addAll(availableList.get(0).getBuyingProducts());
        }
        return nextList;
    }

    private List<SchemeProductBO> checkNextSlabFreeProducts(int schemeID, double qty) {
        List<SchemeBO> availableList = new ArrayList<>();

        List<SchemeProductBO> currentList = new ArrayList<>();
        List<SchemeProductBO> nextList = new ArrayList<>();
        if (qty > 0) {
            boolean isAvailable = false, upFlag = false, downFlag = false;
            int i = 0;
            for (SchemeBO schemeHeader : schemeIDList) {
                if (schemeHeader.getParentId() == schemeID) {
                    availableList.add(schemeHeader);
                }
            }
            for (SchemeBO schemeBO : availableList) {
                if (currentList.size() > 0 && nextList.size() == 0) {
                    isAvailable = true;
                    nextList.addAll(schemeBO.getFreeProducts());
                }
                if (qty >= schemeBO.getFromQty() && qty <= schemeBO.getToQty()) {
                    currentList.addAll(schemeBO.getFreeProducts());
                }
                if (qty >= schemeBO.getFromQty() && qty >= schemeBO.getToQty() && currentList.size() == 0) {
                    upFlag = true;
                }
                if (qty <= schemeBO.getFromQty() && qty <= schemeBO.getToQty() && currentList.size() == 0) {
                    downFlag = true;
                }
                i++;
            }
            if (!isAvailable && upFlag) nextList.addAll(availableList.get(i - 1).getFreeProducts());
            if (!isAvailable && downFlag) nextList.addAll(availableList.get(0).getFreeProducts());
        }
        return nextList;
    }

    private float getPcsPrice(String productID) {
        for (ProductMasterBO product : bModel.productHelper.getProductMaster()) {
            if (product.getProductID().equals(productID)) {
                return product.getSrp();
            }
        }
        return 0;
    }

    private float getCasesPrice(String productID) {
        for (ProductMasterBO product : bModel.productHelper.getProductMaster()) {
            if (product.getProductID().equals(productID)) {
                return product.getCsrp();
            }
        }
        return 0;
    }
}