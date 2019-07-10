package com.ivy.cpg.view.order.scheme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemaQPSAchHistoryBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.view.OnSingleClickListener;

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
    HashMap<Integer, SchemeBO> minItemList = new HashMap<>();
    HashMap<Integer, SchemeBO> preSchemeList;
    HashMap<Integer, SchemeBO> currentSchemeList;
    HashMap<Integer, SchemeBO> nextSchemeList;
    List<SchemeBO> schemeIDList = new ArrayList<>();
    HashMap<Integer, SchemeBO> parentSchemeList;

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
        btnNext.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
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
        applyHeaderLabels();

        btnNext.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
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
                click(2);
            }
        });

        parentSchemeList = new HashMap<>();
        preSchemeList = new HashMap<>();
        currentSchemeList = new HashMap<>();
        nextSchemeList = new HashMap<>();
        mSchemeDoneList = new ArrayList<>();
        schemeHelper.resetSchemeQPSList();

        new SchemeApplyAsync().execute();

        if (!schemeHelper.IS_SCHEME_EDITABLE)
            ((LinearLayout) findViewById(R.id.footer)).setVisibility(View.GONE);
        mExpandableLV.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    mExpandableLV.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Getting scheme applied list by giving product master list
     */
    private class SchemeApplyAsync extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //For setting Ordered SKUs and check Slab in the object.
                List<SchemeBO> tempList = schemeHelper.getSchemeList();
                for (SchemeBO schemeBO : tempList) {
                    if (schemeHelper.getmSchemaQPSAchHistoryList() != null) {
                        if (schemeHelper.getmSchemaQPSAchHistoryList().get(schemeBO.getParentId() + "") != null)
                            schemeIDList.add(schemeBO);
                    }
                }
                checkSlabandsetProduct();
                mSchemeDoneList = buildListView();
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

    private void applyHeaderLabels() {
//        try {
//            ((TextView) findViewById(R.id.tv_schemeduration_header)).setTypeface(
//                    FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
//            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_schemeduration_header).getTag()) != null)
//                ((TextView) findViewById(R.id.tv_schemeduration_header)).setText(bModel.labelsMasterHelper
//                        .applyLabels(findViewById(R.id.tv_schemeduration_header).getTag()));
//        } catch (Exception e) {
//            Commons.printException(e + "");
//        }
        try {
            ((TextView) findViewById(R.id.tv_schemetype_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_schemetype_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_schemetype_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_schemetype_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) findViewById(R.id.tv_cumulative_purchase_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_cumulative_purchase_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_cumulative_purchase_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_cumulative_purchase_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) findViewById(R.id.tv_curslab_sch_amt_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_curslab_sch_amt_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_curslab_sch_amt_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_curslab_sch_amt_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) findViewById(R.id.tv_curslab_rs_per_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_curslab_rs_per_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_curslab_rs_per_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_curslab_rs_per_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) findViewById(R.id.tv_nextslab_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_nextslab_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_nextslab_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_nextslab_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) findViewById(R.id.tv_nextslab_sch_amt_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_nextslab_sch_amt_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_nextslab_sch_amt_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_nextslab_sch_amt_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) findViewById(R.id.tv_nextslab_rs_per_header)).setTypeface(
                    FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
            if (bModel.labelsMasterHelper.applyLabels(findViewById(R.id.tv_nextslab_rs_per_header).getTag()) != null)
                ((TextView) findViewById(R.id.tv_nextslab_rs_per_header)).setText(bModel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.tv_nextslab_rs_per_header).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void checkSlabandsetProduct() {
        minItemList = new HashMap<>();
        HashMap<String, SchemaQPSAchHistoryBO> historyMap = schemeHelper.getmSchemaQPSAchHistoryList();
        for (int i = 0; i < schemeIDList.size(); i++) {
            SchemeBO schemeHeader = schemeIDList.get(i);
            //Min Item to show "next balance to slab" if no Order is taken
            if (minItemList.get(schemeHeader.getParentId()) == null) {
                minItemList.put(schemeHeader.getParentId(), schemeHeader);
            }
            double totalPiecesQty = 0, totalPiecesPriceQty = 0, totalPiecesEveryQty = 0, totalPiecesEveryPriceQty = 0;
            double totalCasesQty = 0, totalCasesPriceQty = 0, totalCasesEveryQty = 0, totalCasesEveryPriceQty = 0;
            for (SchemeProductBO schemeProduct : schemeHeader.getBuyingProducts()) {
                schemeHeader.setFromQty(schemeProduct.getBuyQty());
                schemeHeader.setToQty(schemeProduct.getTobuyQty());
                schemeProduct.setParentID(String.valueOf(schemeHeader.getParentId()));
                schemeProduct.setGetType(schemeHeader.getGetType());
                for (ProductMasterBO product : bModel.productHelper.getProductMaster()) {
                    if (schemeProduct.getProductId().equalsIgnoreCase(product.getProductID())) {
                        schemeHeader.setCaseScheme(product.getCaseUomId() == schemeProduct.getUomID());
                        schemeHeader.setEveryCaseUOM(product.getCaseUomId() == schemeHeader.getEveryUomId());
                        if (schemeHeader.isCaseScheme() && !schemeProduct.getUomDescription().contains("(")) {
                            schemeProduct.setUomDescription(schemeProduct.getUomDescription() + "(" + product.getCaseSize() + ")");
                        }
                        // Order Cases Quantity Calculation
                        if (product.getOrderedCaseQty() > 0 && schemeProduct.getIncreasedCasesQty() == 0 && schemeProduct.getIncreasedPcsQty() == 0) {
                            int everyqty = 0, qty = 0;
                            if (schemeHeader.getEveryQty() > 0) {
                                everyqty = (int) Math.floor(product.getOrderedCaseQty() - (product.getOrderedCaseQty() % schemeHeader.getEveryQty()));
                            }
                            qty = product.getOrderedCaseQty();
                            schemeProduct.setOrderedCasesQty(qty);
                            schemeProduct.setCasesPrice(product.getCsrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                if (schemeHeader.isCaseScheme()) {
                                    totalCasesEveryQty = totalCasesEveryQty + everyqty;
                                    totalCasesEveryPriceQty = totalCasesEveryPriceQty + (product.getCsrp() * everyqty);
                                    totalCasesQty = totalCasesQty + qty;
                                    totalCasesPriceQty = totalCasesPriceQty + (product.getCsrp() * qty);
                                } else {
                                    totalPiecesEveryQty = totalPiecesEveryQty + (everyqty * product.getCaseSize());
                                    totalPiecesEveryPriceQty = totalPiecesEveryPriceQty + (product.getSrp() * (everyqty * product.getCaseSize()));
                                    totalPiecesQty = totalPiecesQty + (qty * product.getCaseSize());
                                    totalPiecesPriceQty = totalPiecesPriceQty + (product.getSrp() * (qty * product.getCaseSize()));
                                }
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * (qty * product.getCaseSize()));
                            }
                        }

                        // Order Pieces Quantity Calculation
                        if (product.getOrderedPcsQty() > 0 && schemeProduct.getIncreasedCasesQty() == 0 && schemeProduct.getIncreasedPcsQty() == 0) {
                            int everyqty = 0, qty = 0;
                            if (schemeHeader.getEveryQty() > 0) {
                                everyqty = (int) Math.floor(product.getOrderedPcsQty() - (product.getOrderedPcsQty() % schemeHeader.getEveryQty()));
                            }
                            qty = product.getOrderedPcsQty();
                            schemeProduct.setOrderedPcsQty(qty);
                            schemeProduct.setPcsPrice(product.getSrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                if (schemeHeader.isCaseScheme()) {
                                    totalCasesEveryQty = totalCasesEveryQty + Math.floor(everyqty / product.getCaseSize());
                                    totalCasesEveryPriceQty = totalCasesEveryPriceQty + (product.getCsrp() * Math.floor(everyqty / product.getCaseSize()));
                                    totalCasesQty = totalCasesQty + Math.floor(qty / product.getCaseSize());
                                    totalCasesPriceQty = totalCasesPriceQty + (product.getCsrp() * Math.floor(qty / product.getCaseSize()));
                                } else {
                                    totalPiecesEveryQty = totalPiecesEveryQty + everyqty;
                                    totalPiecesEveryPriceQty = totalPiecesEveryPriceQty + (product.getSrp() * everyqty);
                                    totalPiecesQty = totalPiecesQty + qty;
                                    totalPiecesPriceQty = totalPiecesPriceQty + (product.getSrp() * qty);
                                }
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * qty);
                            }
                        }

                        // Final Cases Quantity Calculation
                        if (schemeProduct.getIncreasedCasesQty() > 0) {
                            int everyqty = 0, qty = 0;
                            if (schemeHeader.getEveryQty() > 0) {
                                everyqty = (int) Math.floor(schemeProduct.getIncreasedCasesQty() - (schemeProduct.getIncreasedCasesQty() % schemeHeader.getEveryQty()));
                            }
                            qty = schemeProduct.getIncreasedCasesQty();
                            schemeProduct.setCasesPrice(product.getCsrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                if (schemeHeader.isCaseScheme()) {
                                    totalCasesEveryQty = totalCasesEveryQty + everyqty;
                                    totalCasesEveryPriceQty = totalCasesEveryPriceQty + (product.getCsrp() * everyqty);
                                    totalCasesQty = totalCasesQty + qty;
                                    totalCasesPriceQty = totalCasesPriceQty + (product.getCsrp() * qty);
                                } else {
                                    totalPiecesEveryQty = totalPiecesEveryQty + (everyqty * product.getCaseSize());
                                    totalPiecesEveryPriceQty = totalPiecesEveryPriceQty + (product.getSrp() * (everyqty * product.getCaseSize()));
                                    totalPiecesQty = totalPiecesQty + (qty * product.getCaseSize());
                                    totalPiecesPriceQty = totalPiecesPriceQty + (product.getSrp() * (qty * product.getCaseSize()));
                                }
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * (qty * product.getCaseSize()));
                            }
                        }

                        // Final Pieces Quantity Calculation
                        if (schemeProduct.getIncreasedPcsQty() > 0) {
                            int everyqty = 0, qty = 0;
                            if (schemeHeader.getEveryQty() > 0) {
                                everyqty = (int) Math.floor(schemeProduct.getIncreasedPcsQty() - (schemeProduct.getIncreasedPcsQty() % schemeHeader.getEveryQty()));
                            }
                            qty = schemeProduct.getIncreasedPcsQty();
                            schemeProduct.setPcsPrice(product.getSrp());
                            if (schemeHeader.getBuyType().equals("QTY")) {
                                if (schemeHeader.isCaseScheme()) {
                                    totalCasesEveryQty = totalCasesEveryQty + Math.floor(everyqty / product.getCaseSize());
                                    totalCasesEveryPriceQty = totalCasesEveryPriceQty + (product.getCsrp() * Math.floor(everyqty / product.getCaseSize()));
                                    totalCasesQty = totalCasesQty + Math.floor(qty / product.getCaseSize());
                                    totalCasesPriceQty = totalCasesPriceQty + (product.getCsrp() * Math.floor(qty / product.getCaseSize()));
                                } else {
                                    totalPiecesEveryQty = totalPiecesEveryQty + everyqty;
                                    totalPiecesEveryPriceQty = totalPiecesEveryPriceQty + (product.getSrp() * everyqty);
                                    totalPiecesQty = totalPiecesQty + qty;
                                    totalPiecesPriceQty = totalPiecesPriceQty + (product.getSrp() * qty);
                                }
                            } else if (schemeHeader.getBuyType().equals("SV")) {
                                totalPiecesQty = totalPiecesQty + (product.getSrp() * qty);
                            }
                        }
                    }
                }
            }
            //Add Cumulative if exist
            if (historyMap != null && historyMap.get(schemeHeader.getParentId() + "") != null
                    && (totalCasesQty > 0 || totalPiecesQty > 0)) {
                if (schemeHeader.isCaseScheme()) {
                    totalCasesQty = totalCasesQty + historyMap.get(schemeHeader.getParentId() + "").getCumulative_Purchase();
                } else {
                    totalPiecesQty = totalPiecesQty + historyMap.get(schemeHeader.getParentId() + "").getCumulative_Purchase();
                }
            }
            //Check the slab slot and place the object
            if (schemeHeader.isCaseScheme() && totalCasesQty >= schemeHeader.getFromQty() && totalCasesQty <= schemeHeader.getToQty()) {
                schemeHeader.setTotalCaseQty(totalCasesQty);
                if (totalCasesEveryPriceQty > 0) {
                    schemeHeader.setTotalCaseEveryQty(totalCasesEveryQty);
                    schemeHeader.setTotalCasesPriceQty(totalCasesEveryPriceQty);
                } else {
                    schemeHeader.setTotalCasesPriceQty(totalCasesPriceQty);
                }
                currentSchemeList.put(schemeHeader.getParentId(), schemeHeader);
            } else if (totalPiecesQty >= schemeHeader.getFromQty() && totalPiecesQty <= schemeHeader.getToQty()) {
                schemeHeader.setTotalPieceQty(totalPiecesQty);
                if (totalPiecesEveryPriceQty > 0) {
                    schemeHeader.setTotalPieceEveryQty(totalPiecesEveryQty);
                    schemeHeader.setTotalPcsPriceQty(totalPiecesEveryPriceQty);
                } else {
                    schemeHeader.setTotalPcsPriceQty(totalPiecesPriceQty);
                }
                currentSchemeList.put(schemeHeader.getParentId(), schemeHeader);
            } else if (totalPiecesQty > 0 && totalPiecesQty < minItemList.get(schemeHeader.getParentId()).getFromQty()) {
                schemeHeader.setTotalPieceQty(totalPiecesQty);
                preSchemeList.put(schemeHeader.getParentId(), schemeHeader);
            } else if (totalCasesQty > 0 && schemeHeader.isCaseScheme() && totalCasesQty < minItemList.get(schemeHeader.getParentId()).getFromQty()) {
                schemeHeader.setTotalCaseQty(totalCasesQty);
                preSchemeList.put(schemeHeader.getParentId(), schemeHeader);
            }
        }
        //Find the next slab based on current slab
        nextSchemeList.clear();
        if (currentSchemeList.size() == 0 && nextSchemeList.size() == 0) {
            nextSchemeList.putAll(minItemList);
        } else {
            for (int i = 0; i < schemeIDList.size(); i++) {
                if (currentSchemeList.get(schemeIDList.get(i).getParentId()) == null) {
                    nextSchemeList.put(schemeIDList.get(i).getParentId(), minItemList.get(schemeIDList.get(i).getParentId()));
                } else if (currentSchemeList.get(schemeIDList.get(i).getParentId()) != null) {
                    try {
                        if (schemeIDList.get(i + 1) != null && schemeIDList.get(i + 1).getParentId() ==
                                currentSchemeList.get(schemeIDList.get(i).getParentId()).getParentId() &&
                                nextSchemeList.get(schemeIDList.get(i).getParentId()) == null &&
                                currentSchemeList.get(schemeIDList.get(i).getParentId()) != schemeIDList.get(i + 1) &&
                                currentSchemeList.get(schemeIDList.get(i).getParentId()).getToQty() < schemeIDList.get(i + 1).getFromQty()) {
                            nextSchemeList.remove(schemeIDList.get(i).getParentId());
                            nextSchemeList.put(schemeIDList.get(i).getParentId(), schemeIDList.get(i + 1));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private ArrayList<QPSListBO> buildListView() {
        mSchemeDoneList.clear();
        HashMap<String, SchemaQPSAchHistoryBO> historyMap = schemeHelper.getmSchemaQPSAchHistoryList();
        List<ParentSchemeBO> parentSchemeList = schemeHelper.getParentSchemeList();
        for (ParentSchemeBO parentSchemeBO : parentSchemeList) {
            Vector<SchemeProductBO> schemeList = new Vector<>();

            for (Map.Entry<Integer, SchemeBO> entry : currentSchemeList.entrySet()) {
                parentSchemeBO.setFromDate(entry.getValue().getFromDate());
                parentSchemeBO.setToDate(entry.getValue().getToDate());
                if (entry.getKey() == parentSchemeBO.getSchemeID()) {
                    SchemeBO schemeHeader = entry.getValue();
                    if (historyMap != null && historyMap.get(schemeHeader.getParentId() + "") != null) {
                        //Calculating Header Item with Ach History
                        String buyType = "", getType = "";
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            getType = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            parentSchemeBO.setSchemeShortDesc(schemeBO.getGroupName());
                        }

                        //Calculating Static cumulative purchase, Cur Sch Details
                        SchemaQPSAchHistoryBO qpsHistoryBO = historyMap.get(schemeHeader.getParentId() + "");
                        parentSchemeBO.setCumulativePurchase(qpsHistoryBO.getCumulative_Purchase());
                        parentSchemeBO.setCurSlabCumSchAmt(qpsHistoryBO.getCurSlab_Sch_Amt());
                        parentSchemeBO.setCurSlabrsorPer(qpsHistoryBO.getCurSlab_Rs_Per());

                        //Calculating Dynamic cumulative purchase, Cur Sch Details
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            if (buyType.equals("SV")) {
                                parentSchemeBO.setCalculatedCumulativePurchase(/*parentSchemeBO.getCumulativePurchase() + */(schemeHeader.getTotalPieceQty()));
                            } else if (buyType.equals("QTY")) {
                                parentSchemeBO.setCalculatedCumulativePurchase(/*parentSchemeBO.getCumulativePurchase() +*/
                                        (schemeHeader.isCaseScheme() ? schemeHeader.getTotalCaseQty() :
                                                schemeHeader.getTotalPieceQty()));
                            }
                            schemeList.add(schemeBO);
                        }

                        List<SchemeProductBO> calculatedSchemeBOList = checkSlab(entry.getKey(), parentSchemeBO.getCalculatedCumulativePurchase());
                        for (SchemeProductBO schemeBO : calculatedSchemeBOList) {
                            if (getType.equals("PER") || getType.equals("EPERP")) {
                                if (buyType.equals("SV")) { // Amount
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * parentSchemeBO.getCalculatedCumulativePurchase());
                                } else if (buyType.equals("QTY")) { // Qty
                                    double cumulativePurchaseValue = 0;
                                    double x = parentSchemeBO.getCurSlabCumSchAmt() * (100 / parentSchemeBO.getCurSlabrsorPer());
                                    double y = (schemeHeader.isCaseScheme() ? schemeHeader.getTotalCasesPriceQty() : schemeHeader.getTotalPcsPriceQty());
                                    cumulativePurchaseValue = x + y;
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * cumulativePurchaseValue);
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinPercent());
                            } else if (getType.equals("VALUE") || getType.equals("EPER")) {
                                if (buyType.equals("SV")) { //Amount
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt(schemeBO.getMinAmount());
                                } else if (buyType.equals("QTY")) { //Qty
                                    double cumulativePurchaseValue = 0;
                                    if (schemeHeader.getEveryQty() > 0) {
                                        double x = parentSchemeBO.getCumulativePurchase() - (parentSchemeBO.getCurSlabCumSchAmt() % schemeHeader.getEveryQty());
                                        double y = (schemeHeader.isCaseScheme() ? schemeHeader.getTotalCaseEveryQty() : schemeHeader.getTotalPieceEveryQty());
                                        cumulativePurchaseValue = x + y;
                                        parentSchemeBO.setCalculatedcurSlabCumSchAmt(cumulativePurchaseValue * schemeBO.getMinAmount());
                                    } else {
                                        parentSchemeBO.setCalculatedcurSlabCumSchAmt(schemeBO.getMinAmount());
                                    }
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinAmount());
                            }
                        }
                    } else {
                        //Calculating Header Item without Ach History
                        parentSchemeBO.setCumulativePurchase(0);
                        parentSchemeBO.setCurSlabCumSchAmt(0);
                        parentSchemeBO.setCurSlabrsorPer(0);

                        String buyType = "", getType = "";
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            getType = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            if (buyType.equals("SV")) {
                                parentSchemeBO.setCalculatedCumulativePurchase(schemeHeader.getTotalPieceQty());
                            } else {
                                parentSchemeBO.setCalculatedCumulativePurchase(schemeHeader.isCaseScheme() ? schemeHeader.getTotalCaseQty() :
                                        schemeHeader.getTotalPieceQty());
                            }
                            parentSchemeBO.setSchemeShortDesc(schemeBO.getGroupName());
                            schemeList.add(schemeBO);
                        }
                        for (SchemeProductBO schemeBO : schemeHeader.getFreeProducts()) {
                            if (getType.equals("PER") || getType.equals("EPERP")) {
                                if (buyType.equals("SV")) { //Amount
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) * parentSchemeBO.getCalculatedCumulativePurchase());
                                } else if (buyType.equals("QTY")) { //Qty
                                    double cumulativePurchaseValue = 0;
                                    if (schemeHeader.getEveryQty() > 0) {
                                        cumulativePurchaseValue = schemeHeader.isCaseScheme() ? schemeHeader.getTotalCasesPriceQty() : schemeHeader.getTotalPcsPriceQty();
                                        cumulativePurchaseValue = cumulativePurchaseValue * (schemeBO.getMinPercent() / 100);
                                        parentSchemeBO.setCalculatedcurSlabCumSchAmt(cumulativePurchaseValue);
                                    } else {
                                        parentSchemeBO.setCalculatedcurSlabCumSchAmt((schemeBO.getMinPercent() / 100) *
                                                (schemeHeader.isCaseScheme() ? schemeHeader.getTotalCasesPriceQty() : schemeHeader.getTotalPcsPriceQty()));
                                    }
                                }
                                parentSchemeBO.setCalculatedcurSlabrsorPer(schemeBO.getMinPercent());
                            } else if (getType.equals("VALUE") || getType.equals("EPER")) {
                                if (buyType.equals("SV")) { //Amount
                                    parentSchemeBO.setCalculatedcurSlabCumSchAmt(schemeBO.getMinAmount());
                                } else if (buyType.equals("QTY")) { //Qty
                                    double cumulativePurchaseValue = 0;
                                    if (schemeHeader.getEveryQty() > 0) {
                                        cumulativePurchaseValue = schemeHeader.isCaseScheme() ? schemeHeader.getTotalCaseEveryQty() : schemeHeader.getTotalPieceEveryQty();
                                        cumulativePurchaseValue = cumulativePurchaseValue * schemeBO.getMinAmount();
                                        parentSchemeBO.setCalculatedcurSlabCumSchAmt(cumulativePurchaseValue);
                                    } else {
                                        parentSchemeBO.setCalculatedcurSlabCumSchAmt(schemeBO.getMinAmount());
                                    }
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
                    parentSchemeBO.setFromDate(entry.getValue().getFromDate());
                    parentSchemeBO.setToDate(entry.getValue().getToDate());
                    isNextSchemeAvailable = true;
                    SchemeBO schemeHeader = entry.getValue();
                    if (historyMap != null && historyMap.get(schemeHeader.getParentId() + "") != null) {
                        //Calculating Next Slab Item with Ach History
                        String buyType = "";
                        //List<SchemeProductBO> schemeBuyingList = checkNextSlabBuyProducts(entry.getKey(), parentSchemeBO.getCumulativePurchase());
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            parentSchemeBO.setSchemeShortDesc(schemeBO.getGroupName());
                        }
                        SchemaQPSAchHistoryBO qpsHistoryBO = historyMap.get(schemeHeader.getParentId() + "");
                        if (currentSchemeList.get(schemeHeader.getParentId()) == null) {
                            parentSchemeBO.setCumulativePurchase(qpsHistoryBO.getCumulative_Purchase());
                            parentSchemeBO.setCurSlabCumSchAmt(qpsHistoryBO.getCurSlab_Sch_Amt());
                            parentSchemeBO.setCurSlabrsorPer(qpsHistoryBO.getCurSlab_Rs_Per());
                            if (preSchemeList != null && preSchemeList.size() > 0) {
                                try {
                                    SchemeBO scheme = preSchemeList.get(schemeHeader.getParentId());
                                    parentSchemeBO.setCalculatedCumulativePurchase(scheme.getTotalPieceQty());
                                } catch (Exception e) {
                                    parentSchemeBO.setCalculatedCumulativePurchase(0);
                                    e.printStackTrace();
                                }
                            } else {
                                parentSchemeBO.setCalculatedCumulativePurchase(0);
                            }
                            parentSchemeBO.setCalculatedcurSlabCumSchAmt(0);
                            parentSchemeBO.setCalculatedcurSlabrsorPer(0);
                        }
                        parentSchemeBO.setNextSlabBalance(qpsHistoryBO.getNextSlab_balance());
                        parentSchemeBO.setNextSlabCumSchAmt(qpsHistoryBO.getNextSlab_Sch_Amt());
                        parentSchemeBO.setNextSlabrsorPer(qpsHistoryBO.getNextSlab_Rs_Per());

                        schemebuyQty = 0;
                        String getType = "";
                        List<SchemeProductBO> schemenextBuyingList = checkNextSlabBuyProducts(entry.getKey(), parentSchemeBO.getCalculatedCumulativePurchase());
                        for (SchemeProductBO schemeBO : schemenextBuyingList) {
                            getType = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            //if (schemeBO.getBuyType() != null && schemeBO.getBuyType().matches("SV|QTY")) {
                            schemebuyQty = schemeBO.getBuyQty();
                            parentSchemeBO.setCalculatednextSlabBalance(Math.max(0, schemeBO.getBuyQty() - parentSchemeBO.getCalculatedCumulativePurchase()));
                            //}
                            if (currentSchemeList.get(schemeHeader.getParentId()) == null) {
                                schemeList.add(schemeBO);
                            }
                        }

                        if (parentSchemeBO.getCalculatednextSlabBalance() > 0) {
                            List<SchemeProductBO> nextSchemeBOList = checkNextSlabFreeProducts(entry.getKey(), parentSchemeBO.getCalculatedCumulativePurchase());
                            for (SchemeProductBO schemeBO : nextSchemeBOList) {
                                if (getType.equals("PER") || getType.equals("EPERP")) {
                                    parentSchemeBO.setCalculatednextSlabCumSchAmt((buyType.equals("SV")) ? ((schemeBO.getMinPercent() / 100) * schemebuyQty) : 0);
                                    parentSchemeBO.setCalculatednextSlabrsorPer(schemeBO.getMinPercent());
                                } else if (getType.equals("VALUE") || getType.equals("EPER")) {
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
                        if (currentSchemeList.get(schemeHeader.getParentId()) == null) {
                            if (preSchemeList != null && preSchemeList.size() > 0) {
                                try {
                                    SchemeBO scheme = preSchemeList.get(schemeHeader.getParentId());
                                    parentSchemeBO.setCalculatedCumulativePurchase(scheme.getTotalPieceQty());
                                } catch (Exception e) {
                                    parentSchemeBO.setCalculatedCumulativePurchase(0);
                                    e.printStackTrace();
                                }
                            } else {
                                parentSchemeBO.setCalculatedCumulativePurchase(0);
                            }
                            parentSchemeBO.setCalculatedcurSlabCumSchAmt(0);
                            parentSchemeBO.setCalculatedcurSlabrsorPer(0);
                        }
                        parentSchemeBO.setNextSlabBalance(0);
                        parentSchemeBO.setNextSlabCumSchAmt(0);
                        parentSchemeBO.setNextSlabrsorPer(0);

                        String buyType = "", getType = "";
                        for (SchemeProductBO schemeBO : schemeHeader.getBuyingProducts()) {
                            buyType = (schemeBO.getBuyType() != null) ? schemeBO.getBuyType() : "";
                            getType = (schemeBO.getGetType() != null) ? schemeBO.getGetType() : "";
                            parentSchemeBO.setSchemeShortDesc(schemeBO.getGroupName());
                            if (schemeBO.getBuyType().matches("SV|QTY")) {
                                schemebuyQty = schemeBO.getBuyQty();
                                parentSchemeBO.setCalculatednextSlabBalance(Math.max(0, schemeBO.getBuyQty() - parentSchemeBO.getCalculatedCumulativePurchase()));
                            }
                            if (currentSchemeList.get(schemeHeader.getParentId()) == null) {
                                schemeList.add(schemeBO);
                            }
                        }
                        for (SchemeProductBO schemeBO : schemeHeader.getFreeProducts()) {
                            if (getType.equals("PER") || getType.equals("EPERP")) {
                                parentSchemeBO.setCalculatednextSlabCumSchAmt((buyType.equals("SV")) ? (schemeBO.getMinPercent() / 100) * schemebuyQty : 0);
                                parentSchemeBO.setCalculatednextSlabrsorPer(schemeBO.getMinPercent());
                            } else if (getType.equals("VALUE") || getType.equals("EPER")) {
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
            if (parentSchemeBO.getCalculatedcurSlabCumSchAmt() > 0) {
                parentSchemeBO.setSelected(true);
            } else {
                parentSchemeBO.setSelected(false);
            }
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
//                    Intent intent = new Intent(QPSSchemeApply.this,
//                            CrownReturnActivity.class);
//                    intent.putExtra("OrderFlag", "Nothing");
//                    intent.putExtra("ScreenCode", screenCode);
//                    startActivity(intent);
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
//            if (view == null) {
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
            holder.tv_productName.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
            holder.tv_pcs_ordered_qty.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
            holder.tv_cases_ordered_qty.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
            holder.tv_uom.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
            holder.tv_pcs_final_qty.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
            holder.tv_cases_final_qty.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
            holder.tv_gqt.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));

            try {
                ((TextView) view.findViewById(R.id.tv_header_productName)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_productName).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_productName)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_productName).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            try {
                ((TextView) view.findViewById(R.id.tv_header_pcs_ordered_qty)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_pcs_ordered_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_pcs_ordered_qty)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_pcs_ordered_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            try {
                ((TextView) view.findViewById(R.id.tv_header_cases_ordered_qty)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_cases_ordered_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_cases_ordered_qty)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_cases_ordered_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            try {
                ((TextView) view.findViewById(R.id.tv_header_uom)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_uom).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_uom)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_uom).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            try {
                ((TextView) view.findViewById(R.id.tv_header_pcs_final_qty)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_pcs_final_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_pcs_final_qty)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_pcs_final_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            try {
                ((TextView) view.findViewById(R.id.tv_header_cases_final_qty)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_cases_final_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_cases_final_qty)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_cases_final_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            try {
                ((TextView) view.findViewById(R.id.tv_header_gqt)).setTypeface(
                        FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.tv_header_gqt).getTag()) != null)
                    ((TextView) view.findViewById(R.id.tv_header_gqt)).setText(bModel.labelsMasterHelper
                            .applyLabels(view.findViewById(R.id.tv_header_gqt).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }

            holder.tv_pcs_final_qty.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    QUANTITY = holder.tv_pcs_final_qty;
                    QUANTITY.setTag(holder.schemeProducts.getParentID() + "," + holder.schemeProducts.getProductId() + ",0");
                    int inType = holder.tv_pcs_final_qty.getInputType();
                    holder.tv_pcs_final_qty.setInputType(InputType.TYPE_NULL);
                    holder.tv_pcs_final_qty.onTouchEvent(motionEvent);
                    holder.tv_pcs_final_qty.setInputType(inType);
                    holder.tv_pcs_final_qty.selectAll();
                    holder.tv_pcs_final_qty.setFocusable(true);
                    holder.tv_pcs_final_qty.requestFocus();
                    return true;
                }
            });
            holder.tv_pcs_final_qty.addTextChangedListener(new CustomTextWatcher(holder.tv_pcs_final_qty) {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().equals(holder.schemeProducts.getIncreasedPcsQty() + "")) {
                        schemeHelper.resetSchemeQPSListforData();
                        for (SchemeBO scheme : schemeIDList) {
                            List<SchemeProductBO> productList = scheme.getBuyingProducts();
                            for (SchemeProductBO product : productList) {
                                if (product.getProductId().equals(holder.schemeProducts.getProductId())) {
                                    product.setIncreasedPcsQty(SDUtil.convertToInt(s.toString()));
                                }
                            }
                        }
                        currentSchemeList = new HashMap<>();
                        nextSchemeList = new HashMap<>();
                        checkSlabandsetProduct();
                        buildListView();
                        mExpandableAdapterNew.notifyDataSetChanged();
                    }
                }
            });
            holder.tv_cases_final_qty.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    QUANTITY = holder.tv_cases_final_qty;
                    QUANTITY.setTag(holder.schemeProducts.getParentID() + "," + holder.schemeProducts.getProductId() + ",1");
                    int inType = holder.tv_cases_final_qty.getInputType();
                    holder.tv_cases_final_qty.setInputType(InputType.TYPE_NULL);
                    holder.tv_cases_final_qty.onTouchEvent(motionEvent);
                    holder.tv_cases_final_qty.setInputType(inType);
                    holder.tv_cases_final_qty.selectAll();
                    holder.tv_cases_final_qty.setFocusable(true);
                    holder.tv_cases_final_qty.requestFocus();
                    return true;
                }
            });
            holder.tv_cases_final_qty.addTextChangedListener(new CustomTextWatcher(holder.tv_cases_final_qty) {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().equals(holder.schemeProducts.getIncreasedCasesQty() + "")) {
                        schemeHelper.resetSchemeQPSListforData();
                        for (SchemeBO scheme : schemeIDList) {
                            List<SchemeProductBO> productList = scheme.getBuyingProducts();
                            for (SchemeProductBO product : productList) {
                                if (product.getProductId().equals(holder.schemeProducts.getProductId())) {
                                    product.setIncreasedCasesQty(SDUtil.convertToInt(s.toString()));
                                }
                            }
                        }
                        currentSchemeList = new HashMap<>();
                        nextSchemeList = new HashMap<>();
                        checkSlabandsetProduct();
                        buildListView();
                        mExpandableAdapterNew.notifyDataSetChanged();
                    }
                }
            });
            view.setTag(holder);
//            } else {
//                holder = (SchemeProductDetailHolder) view.getTag();
//            }

            holder.schemeProducts = (SchemeProductBO) getChild(groupPosition,
                    childPosition);

            if (childPosition != 0) {
                holder.lnrSchemeHeader.setVisibility(View.GONE);
            } else {
                holder.lnrSchemeHeader.setVisibility(View.VISIBLE);
            }
            holder.tv_productName.setText(holder.schemeProducts.getProductName().length() == 0 ?
                    holder.schemeProducts.getProductFullName() : holder.schemeProducts.getProductName());
            holder.tv_uom.setText(holder.schemeProducts.getUomDescription());
            holder.tv_pcs_ordered_qty.setText(holder.schemeProducts.getOrderedPcsQty() + "");
            holder.tv_cases_ordered_qty.setText(holder.schemeProducts.getOrderedCasesQty() + "");
            String gqt = "";
            if (holder.schemeProducts.getIncreasedPcsQty() == 0 && holder.schemeProducts.getIncreasedCasesQty() == 0) {
                gqt = (holder.schemeProducts.getOrderedPcsQty() + (holder.schemeProducts.getOrderedCasesQty() * holder.schemeProducts.getCasesPrice())) + "";
            } else {
                gqt = (holder.schemeProducts.getIncreasedPcsQty() + (holder.schemeProducts.getIncreasedCasesQty() * holder.schemeProducts.getCasesPrice())) + "";
            }
            holder.tv_gqt.setText(SDUtil.getWithoutExponential(gqt));
            holder.tv_pcs_final_qty.setText(holder.schemeProducts.getIncreasedPcsQty() + "");
            holder.tv_cases_final_qty.setText(holder.schemeProducts.getIncreasedCasesQty() + "");

            try {
                if (QUANTITY != null && QUANTITY.getTag() != null) {
                    String[] tag = QUANTITY.getTag().toString().split(",");
                    if (tag[0].equals(holder.schemeProducts.getParentID()) && tag[1].equals(holder.schemeProducts.getProductId())) {
                        if (tag[2].equals("0")) {
                            QUANTITY = holder.tv_pcs_final_qty;
                            QUANTITY.setTag(holder.schemeProducts.getParentID() + "," +
                                    holder.schemeProducts.getProductId() + "," + "0");
                        } else if (tag[2].equals("1")) {
                            QUANTITY = holder.tv_cases_final_qty;
                            QUANTITY.setTag(holder.schemeProducts.getParentID() + "," +
                                    holder.schemeProducts.getProductId() + "," + "1");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error " + e.toString());
                System.out.println("Focus on Error " + groupPosition + "," + childPosition);
                e.printStackTrace();
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
                holder.tv_scheme_secondary_title = view.findViewById(R.id.tv_scheme_secondary_title);
//                holder.tv_schemeDuration = view
//                        .findViewById(R.id.tv_schemeduration);
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
                holder.qpsSchemeHeaderLayout = (CardView) view.findViewById(R.id.qpsSchemeHeaderLayout);
                //typeface
                //typeface
                holder.tv_scheme.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_scheme_secondary_title.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                //holder.tv_schemeDuration.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, QPSSchemeApply.this));
                holder.tv_schemeType.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_cumulative_purchase.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_curslab_sch_amt.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_curslab_rs_per.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_nextslab.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_nextslab_sch_amt.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_nextslab_rs_per.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));

                holder.tv_calculated_cumulative_purchase.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_calculated_curslab_sch_amt.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_calculated_curslab_rs_per.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_calculated_nextslab.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_calculated_nextslab_sch_amt.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                holder.tv_calculated_nextslab_rs_per.setTypeface(FontUtils.getFontRoboto(QPSSchemeApply.this, FontUtils.FontType.MEDIUM));
                view.setTag(holder);
            } else {
                holder = (SchemeProductHolder) view.getTag();
            }

            holder.parentSchemeBO = mSchemeDoneList.get(groupPosition).getParentScheme();
            holder.tv_scheme.setText(holder.parentSchemeBO.getSchemeDesc());
            //holder.tv_scheme_secondary_title.setText(holder.parentSchemeBO.getSchemeShortDesc());
            String periodStart = DateTimeUtils.convertFromServerDateToRequestedFormat(holder.parentSchemeBO.getFromDate(),"dd-MM-yyyy");
            String periodEnd = DateTimeUtils.convertFromServerDateToRequestedFormat(holder.parentSchemeBO.getToDate(),"dd-MM-yyyy");
            holder.tv_scheme_secondary_title.setText(periodStart + " to " + periodEnd);
            holder.tv_schemeType.setText(holder.parentSchemeBO.getBuyType());

            holder.tv_cumulative_purchase.setText(SDUtil.getWithoutExponential(holder.parentSchemeBO.getCumulativePurchase()) + "");
            holder.tv_curslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getCurSlabCumSchAmt(), 2, 0) + "");
            holder.tv_curslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getCurSlabrsorPer(), 2, 0) + "");
            holder.tv_nextslab.setText(SDUtil.getWithoutExponential(holder.parentSchemeBO.getNextSlabBalance()) + "");
            holder.tv_nextslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getNextSlabCumSchAmt(), 2, 0) + "");
            holder.tv_nextslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getNextSlabrsorPer(), 2, 0) + "");

            holder.tv_calculated_cumulative_purchase.setText(SDUtil.getWithoutExponential(holder.parentSchemeBO.getCalculatedCumulativePurchase()) + "");
            holder.tv_calculated_curslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getCalculatedcurSlabCumSchAmt(), 2, 0) + "");
            holder.tv_calculated_curslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getCalculatedcurSlabrsorPer(), 2, 0) + "");
            holder.tv_calculated_nextslab.setText(SDUtil.getWithoutExponential(holder.parentSchemeBO.getCalculatednextSlabBalance()) + "");
            holder.tv_calculated_nextslab_sch_amt.setText(SDUtil.format(holder.parentSchemeBO.getCalculatednextSlabCumSchAmt(), 2, 0) + "");
            holder.tv_calculated_nextslab_rs_per.setText(SDUtil.format(holder.parentSchemeBO.getCalculatednextSlabrsorPer(), 2, 0) + "");
            if (holder.parentSchemeBO.isSelected()) {
                holder.qpsSchemeHeaderLayout.setCardBackgroundColor(Color.parseColor("#196F3D"));
                holder.tv_scheme_secondary_title.setTextColor(Color.WHITE);
            } else {
                holder.qpsSchemeHeaderLayout.setCardBackgroundColor(Color.WHITE);
                holder.tv_scheme_secondary_title.setTextColor(Color.parseColor("#175fab"));
            }
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
        private TextView tv_scheme_secondary_title;
        //private TextView tv_schemeDuration;
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

        private CardView qpsSchemeHeaderLayout;
    }

    static class SchemeProductDetailHolder {
        SchemeProductBO schemeProducts;
        TextView tv_productName, tv_pcs_ordered_qty, tv_cases_ordered_qty,
                tv_uom, tv_gqt;
        EditText tv_pcs_final_qty, tv_cases_final_qty;
        LinearLayout lnrSchemeHeader;
        //int group, child;
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
        //if (qty > 0) {
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
        //}
        return nextList;
    }

    private List<SchemeProductBO> checkNextSlabFreeProducts(int schemeID, double qty) {
        List<SchemeBO> availableList = new ArrayList<>();

        List<SchemeProductBO> currentList = new ArrayList<>();
        List<SchemeProductBO> nextList = new ArrayList<>();
        //if (qty > 0) {
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
        //}
        return nextList;
    }

    private class CustomTextWatcher implements TextWatcher {
        private EditText mEditText;

        public CustomTextWatcher(EditText et) {
            mEditText = et;
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        public void afterTextChanged(Editable s) {
        }
    }

}