package com.ivy.cpg.view.order.catalog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.order.catalog.CatalogOrderValueUpdate;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

/**
 * Created by subramanian.r on 23-03-2016.
 */
public class CustomKeyBoardCatalog extends Dialog implements View.OnClickListener {

    private TextView case_typed_value, outer_case_typed_value, pcs_typed_value;

    private Button number_one, number_two, number_three;
    private Button number_four, number_five, number_six;
    private Button number_seven, number_eight, number_nine;
    private Button number_zero, decimal_point;

    private Button btn_cancel, btn_ok;
    private ImageButton btn_delete;
    private String casevalue = "0", outervalue = "0", pcsvalue = "0";

    private TextView value_keyboard, total_tv, so_keyboard, tv_totalStockQty;
    private CatalogOrderValueUpdate catalogOrderValueUpdate;
    private BusinessModel bmodel;

    public boolean isDialogCreated() {
        return isDialogCreated;
    }

    private boolean isDialogCreated;
    private int limit = 6; // default 6 charteres can entered
    private ProductMasterBO pdtBO;
    private Button orderBtn;
    private Context context;
    private boolean isOrderAllowed = true;
    //user for quantity values
    TextView selectedTextView;
    LinearLayout llCase, llOuter, llPiece;

    private int currentPsQty = 0;
    private int currentCsQty = 0;
    private int currentOuQty = 0;

    public CustomKeyBoardCatalog(Context context, TextView total_tv, Button orderBtn, ProductMasterBO pdtBO, BusinessModel bmodel, boolean isDecimalAllowed) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_keyboard_catalog);
        TextView pdtNameTV = (TextView) findViewById(R.id.pdt_name_keyboard);
        TextView SIHTV = (TextView) findViewById(R.id.sih_keyboard);
        TextView psqTV = findViewById(R.id.psq_keyboard);
        LinearLayout cust_layout = (LinearLayout) findViewById(R.id.cust_layout);

        TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        this.context = context;
        this.catalogOrderValueUpdate = (CatalogOrderValueUpdate) context;
        this.pdtBO = pdtBO;
        this.bmodel = bmodel;
        this.value_keyboard = (TextView) findViewById(R.id.value_keyboard);
        this.so_keyboard = (TextView) findViewById(R.id.so_keyboard);
        this.tv_totalStockQty = findViewById(R.id.total_stock_keyboard);
        this.total_tv = total_tv;
        this.orderBtn = orderBtn;

        cust_layout.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
        pdtNameTV.setText(pdtBO.getProductName());
        SIHTV.setText("SIH : " + pdtBO.getSIH());
        so_keyboard.setText("SO : " + pdtBO.getSoInventory());

        isDialogCreated = true;

        getProductTotalValue();

        initializeViews();
        if (total_tv == null) {
            value_keyboard.setVisibility(View.GONE);
            //setKeyboard(((pdtBO.downloadInStoreLocationsForStockCheck().get(0).getShelfPiece() != -1) ? pdtBO.downloadInStoreLocationsForStockCheck().get(0).getShelfPiece() : 0) + "");
            setCaseKeyboard(((pdtBO.getLocations().get(0).getShelfCase() != -1) ? pdtBO.getLocations().get(0).getShelfCase() : 0) + "", true);
            setOuterKeyboard(((pdtBO.getLocations().get(0).getShelfOuter() != -1) ? pdtBO.getLocations().get(0).getShelfOuter() : 0) + "", true);
            setPcsKeyboard(((pdtBO.getLocations().get(0).getShelfPiece() != -1) ? pdtBO.getLocations().get(0).getShelfPiece() : 0) + "", true);
        } else {
            value_keyboard.setText(context.getResources().getString(R.string.value) + " : " + bmodel.formatValue(pdtBO.getTotalamount()));
            //mSelectedTV = tv;

            setCaseKeyboard(pdtBO.getOrderedCaseQty() + "", false);
            setOuterKeyboard(pdtBO.getOrderedOuterQty() + "", false);
            setPcsKeyboard(pdtBO.getOrderedPcsQty() + "", false);
        }
        if (isDecimalAllowed) {
            decimal_point.setVisibility(View.VISIBLE);
            decimal_point.setClickable(true);
            decimal_point.setText(".");
            decimal_point.setOnClickListener(this);
        }
        if (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER)
            so_keyboard.setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
            SIHTV.setVisibility(View.GONE);
        } else {
            if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    SIHTV.setVisibility(View.GONE);
                } else {
                    SIHTV.setVisibility(View.VISIBLE);
                }
            } else {
                SIHTV.setVisibility(View.VISIBLE);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_TOTAL && total_tv != null) {
            total_tv.setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_PSQ) {
            psqTV.setVisibility(View.VISIBLE);
            String strPSQ;
            if (bmodel.labelsMasterHelper
                    .applyLabels("psq") != null) {
                strPSQ = bmodel.labelsMasterHelper
                        .applyLabels("psq") + ": "
                        + pdtBO.getRetailerWiseP4StockQty() + "";
            } else {
                strPSQ = context.getResources().getString(R.string.psq) + ": "
                        + pdtBO.getRetailerWiseP4StockQty();
            }
            psqTV.setText(strPSQ);
        } else {
            psqTV.setVisibility(View.GONE);
        }

        if (!bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER || total_tv != null)
            tv_totalStockQty.setVisibility(View.GONE);
        tv_totalStockQty.setText(hasStockChecked() ? String.valueOf(pdtBO.getTotalStockQty()) : " --");
    }

    private void initializeViews() {
        case_typed_value = (TextView) findViewById(R.id.case_typed_value);
        outer_case_typed_value = (TextView) findViewById(R.id.outer_case_typed_value);
        pcs_typed_value = (TextView) findViewById(R.id.pcs_typed_value);

        number_one = (Button) findViewById(R.id.num_one);
        number_two = (Button) findViewById(R.id.num_two);
        number_three = (Button) findViewById(R.id.num_three);
        number_four = (Button) findViewById(R.id.num_four);
        number_five = (Button) findViewById(R.id.num_five);
        number_six = (Button) findViewById(R.id.num_six);
        number_seven = (Button) findViewById(R.id.num_seven);
        number_eight = (Button) findViewById(R.id.num_eight);
        number_nine = (Button) findViewById(R.id.num_nine);
        number_zero = (Button) findViewById(R.id.num_zero);
        decimal_point = (Button) findViewById(R.id.dec_dot);


        btn_cancel = (Button) findViewById(R.id.cancel);
        btn_ok = (Button) findViewById(R.id.ok);
        btn_delete = (ImageButton) findViewById(R.id.delete);
        btn_cancel.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btn_ok.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        number_one.setOnClickListener(this);
        number_two.setOnClickListener(this);
        number_three.setOnClickListener(this);
        number_four.setOnClickListener(this);
        number_five.setOnClickListener(this);
        number_six.setOnClickListener(this);
        number_seven.setOnClickListener(this);
        number_eight.setOnClickListener(this);
        number_nine.setOnClickListener(this);
        number_zero.setOnClickListener(this);
        //decimal_point.setOnClickListener(this);

        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

        llCase = (LinearLayout) findViewById(R.id.llCase);
        llOuter = (LinearLayout) findViewById(R.id.llOuter);
        llPiece = (LinearLayout) findViewById(R.id.llPcs);

        String caseTitleText = "";
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            llCase.setVisibility(View.GONE);
        } else {
            try {
                ((TextView) findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.caseTitle).getTag()) != null) {
                    ((TextView) findViewById(R.id.caseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.caseTitle).getTag()));
                    caseTitleText = bmodel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.caseTitle).getTag());
                } else
                    caseTitleText = getContext().getResources().getString(R.string.item_case);
            } catch (Exception e) {
                caseTitleText = getContext().getResources().getString(R.string.item_case);
                Commons.printException(e + "");
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE && pdtBO.getCaseSize() > 0) {
            String label = caseTitleText + "(" + pdtBO.getCaseSize() + getContext().getResources().getQuantityString(R.plurals.pcs, pdtBO.getCaseSize()) + ")";
            ((TextView) findViewById(R.id.caseTitle)).setText(label);
        }

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            llPiece.setVisibility(View.GONE);
        } else {
            try {
                ((TextView) findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.pcsTitle).getTag()));
                else
                    ((TextView) findViewById(R.id.pcsTitle))
                            .setText(getContext().getResources().getString(R.string.item_piece));
            } catch (Exception e) {
                ((TextView) findViewById(R.id.pcsTitle))
                        .setText(getContext().getResources().getString(R.string.item_piece));
                Commons.printException(e + "");
            }
        }


        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
            llOuter.setVisibility(View.GONE);
        } else {
            try {
                ((TextView) findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outercaseTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.outercaseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.outercaseTitle)
                                            .getTag()));
                else
                    ((TextView) findViewById(R.id.outercaseTitle))
                            .setText(getContext().getResources().getString(R.string.item_outer));
            } catch (Exception e) {
                ((TextView) findViewById(R.id.outercaseTitle))
                        .setText(getContext().getResources().getString(R.string.item_outer));
                Commons.printException(e + "");
            }
        }
    }

    private void setCaseKeyboard(String s, boolean isStock) {

        /**
         * set max length digit based on IS_ORD_DIGIT config initially it will allow 4 digit only
         */
        if (bmodel.configurationMasterHelper.IS_ORD_DIGIT && !isStock)
            case_typed_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.ORD_DIGIT)});
        if (bmodel.configurationMasterHelper.IS_STK_DIGIT && isStock)
            case_typed_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.STK_DIGIT)});

        currentCsQty = pdtBO.getOrderedCaseQty();

        if (s.length() > 0)
            this.casevalue = s;

        if (s.equals("-1"))
            casevalue = "0";

        case_typed_value.setText(casevalue);

        if (pdtBO.getCaseUomId() == 0 || !pdtBO.isCaseMapped()) {
            case_typed_value.setEnabled(false);
        } else {
            case_typed_value.setEnabled(true);
        }

        case_typed_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedTextView = case_typed_value;
                setColor();
            }
        });

        case_typed_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String qty = s.toString();

                if (total_tv != null) {

                    float totalQty = (SDUtil.convertToInt(qty) * pdtBO.getCaseSize())
                            + (pdtBO.getOrderedPcsQty())
                            + (pdtBO.getOrderedOuterQty() * pdtBO.getOutersize());

                    if (pdtBO.isAllocation() == 1 && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if (totalQty <= pdtBO.getSIH()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                            //double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context, String.format(context.getResources().getString(R.string.exceed),
                                        pdtBO.getSIH()), Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                                pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                            }
                        }
                    } else if (pdtBO.isCbsihAvailable()) {
                        if (totalQty <= pdtBO.getCpsih()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                            //double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context, String.format(context.getResources().getString(R.string.exceed),
                                        pdtBO.getCpsih()), Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                                pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                            }
                        }
                    } else {
                        isOrderAllowed = true;
                        if (!qty.equals("")) {
                            pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                        }
                        double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                        //double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getSrp());
                        pdtBO.setTotalamount(tot);
                    }
                } else {
                    if (!qty.equals("")) {
                        if (qty.endsWith("."))
                            qty = qty.substring(0, qty.length() - 1);
                        if (qty.equals("-1"))
                            pdtBO.getLocations().get(0).setShelfCase(-1);
                        else
                            pdtBO.getLocations().get(0).setShelfCase(SDUtil.convertToInt(qty));

                        if (bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER
                                && (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC
                                || bmodel.configurationMasterHelper.getSOLogic() != 1)) {

                            String totalStockInPiece = getProductTotalValue();
                            tv_totalStockQty.setText(totalStockInPiece);

                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setOuterKeyboard(String s, boolean isStock) {

        /**
         * set max length digit based on IS_ORD_DIGIT config initially it will allow 4 digit only
         */
        if (bmodel.configurationMasterHelper.IS_ORD_DIGIT && !isStock)
            outer_case_typed_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.ORD_DIGIT)});
        if (bmodel.configurationMasterHelper.IS_STK_DIGIT && isStock)
            outer_case_typed_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.STK_DIGIT)});

        currentOuQty = pdtBO.getOrderedOuterQty();
        if (s.length() > 0)
            this.outervalue = s;

        if (s.equals("-1"))
            outervalue = "0";

        outer_case_typed_value.setText(outervalue);

        if (pdtBO.getOuUomid() == 0 || !pdtBO.isOuterMapped()) {
            outer_case_typed_value.setEnabled(false);
        } else {
            outer_case_typed_value.setEnabled(true);
        }

        outer_case_typed_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedTextView = outer_case_typed_value;
                setColor();
            }
        });

        outer_case_typed_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String qty = s.toString();
                if (total_tv != null) {
                    float totalQty = (SDUtil.convertToInt(qty) * pdtBO.getOutersize())
                            + (pdtBO.getOrderedCaseQty() * pdtBO.getCaseSize())
                            + (pdtBO.getOrderedPcsQty());

                    if (pdtBO.isAllocation() == 1 && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if (totalQty <= pdtBO.getSIH()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                            //double tot = (pdtBO.getOrderedOuterQty() * pdtBO.getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context, String.format(context.getResources().getString(R.string.exceed),
                                        pdtBO.getSIH()), Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                                pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                            }
                        }
                    } else if (pdtBO.isCbsihAvailable()) {
                        if (totalQty <= pdtBO.getCpsih()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                            //double tot = (pdtBO.getOrderedOuterQty() * pdtBO.getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context, String.format(context.getResources().getString(R.string.exceed),
                                        pdtBO.getCpsih()), Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                                pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                            }
                        }
                    } else {
                        isOrderAllowed = true;
                        if (!qty.equals("")) {
                            pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                        }
                        double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                        //double tot = (pdtBO.getOrderedOuterQty() * pdtBO.getSrp());
                        pdtBO.setTotalamount(tot);
                    }
                } else {

                    if (!qty.equals("")) {
                        if (qty.endsWith("."))
                            qty = qty.substring(0, qty.length() - 1);
                        if (qty.equals("-1"))
                            pdtBO.getLocations().get(0).setShelfOuter(-1);
                        else
                            pdtBO.getLocations().get(0).setShelfOuter(SDUtil.convertToInt(qty));

                        if (!qty.equals("") && bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER
                                && (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC
                                || bmodel.configurationMasterHelper.getSOLogic() != 1)) {

                            String totalStockInPiece = getProductTotalValue();
                            tv_totalStockQty.setText(totalStockInPiece);

                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setPcsKeyboard(String s, boolean isStock) {

        /**
         * set max length digit based on IS_ORD_DIGIT config initially it will allow 4 digit only
         */
        if (bmodel.configurationMasterHelper.IS_ORD_DIGIT && !isStock)
            pcs_typed_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.ORD_DIGIT)});
        if (bmodel.configurationMasterHelper.IS_STK_DIGIT && isStock)
            pcs_typed_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.STK_DIGIT)});

        currentPsQty = pdtBO.getOrderedPcsQty();
        if (s.length() > 0)
            this.pcsvalue = s;

        if (s.equals("-1"))
            pcsvalue = "0";

        pcs_typed_value.setText(pcsvalue);

        if (pdtBO.getPcUomid() == 0 || !pdtBO.isPieceMapped()) {
            pcs_typed_value.setEnabled(false);
        } else {
            pcs_typed_value.setEnabled(true);
        }

        pcs_typed_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedTextView = pcs_typed_value;
                setColor();
            }
        });

        pcs_typed_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String qty = s.toString();

                if (total_tv != null) {
                    float totalQty = (pdtBO.getOrderedCaseQty() * pdtBO.getCaseSize())
                            + (SDUtil.convertToInt(qty))
                            + (pdtBO.getOrderedOuterQty() * pdtBO.getOutersize());

                    if (pdtBO.isAllocation() == 1 && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if (totalQty <= pdtBO.getSIH()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                            //double tot = (pdtBO.getOrderedPcsQty() * pdtBO.getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context, String.format(context.getResources().getString(
                                        R.string.exceed),
                                        pdtBO.getSIH()), Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                                pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                            }
                        }
                    } else if (pdtBO.isCbsihAvailable()) {
                        if (totalQty <= pdtBO.getCpsih()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                            //double tot = (pdtBO.getOrderedPcsQty() * pdtBO.getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context, String.format(context.getResources().getString(
                                        R.string.exceed),
                                        pdtBO.getCpsih()), Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                                pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                            }
                        }
                    } else {
                        isOrderAllowed = true;
                        if (!qty.equals("")) {
                            pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                        }
                        double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                                + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                                + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                        //double tot = (pdtBO.getOrderedPcsQty() * pdtBO.getSrp());
                        pdtBO.setTotalamount(tot);
                    }
                } else {
                    if (!qty.equals("")) {

                        if (qty.endsWith("."))
                            qty = qty.substring(0, qty.length() - 1);
                        if (qty.equals("-1"))
                            pdtBO.getLocations().get(0).setShelfPiece(-1);
                        else
                            pdtBO.getLocations().get(0).setShelfPiece(SDUtil.convertToInt(qty));

                        if (!qty.equals("") && bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER
                                && (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC
                                || bmodel.configurationMasterHelper.getSOLogic() != 1)) {

                            String totalStockInPiece = getProductTotalValue();
                            tv_totalStockQty.setText(totalStockInPiece);

                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ok) {
            //update value
            setValues();
        } else if (id == R.id.cancel) {
            pdtBO.setOrderedPcsQty(currentPsQty);
            pdtBO.setOrderedCaseQty(currentCsQty);
            pdtBO.setOrderedOuterQty(currentOuQty);
            dismiss();
            isDialogCreated = false;
            if (total_tv != null && total_tv.getText().toString().equals("0")) {
                catalogOrderValueUpdate.updateTotalValue(pdtBO.getOrderedPcsQty() + "");
            }
        } else if (id == R.id.delete) {
            if (selectedTextView == null) {
                Toast.makeText(context, "Please Select an Item", Toast.LENGTH_LONG).show();
            } else {
                String value = selectedTextView.getText().toString();
                value = value.substring(0, value.length() - 1);
                if (value.equals(""))
                    value = "0";
                selectedTextView.setText(value);
                if (total_tv != null) {
                    updateTotalValue("DEL");
                }
            }
        } else {
            if (isOrderAllowed) {
                addValue(v.getTag().toString());
            }

        }
    }

    private void setValues() {

        if (total_tv == null) {
            //mSelectedView.setText(value);
            calculateSONew();

            String caseValue = case_typed_value.getText().toString();
            if (caseValue.endsWith("."))
                caseValue = caseValue.substring(0, caseValue.length() - 1);
            if (caseValue.equals("-1"))
                pdtBO.getLocations().get(0).setShelfCase(-1);
            else
                pdtBO.getLocations().get(0).setShelfCase(SDUtil.convertToInt(caseValue));

            String outerValue = outer_case_typed_value.getText().toString();
            if (outerValue.endsWith("."))
                outerValue = outerValue.substring(0, outerValue.length() - 1);
            if (outerValue.equals("-1"))
                pdtBO.getLocations().get(0).setShelfOuter(-1);
            else
                pdtBO.getLocations().get(0).setShelfOuter(SDUtil.convertToInt(outerValue));

            String pcsValue = pcs_typed_value.getText().toString();
            if (pcsValue.endsWith("."))
                pcsValue = pcsValue.substring(0, pcsValue.length() - 1);
            if (pcsValue.equals("-1"))
                pdtBO.getLocations().get(0).setShelfPiece(-1);
            else
                pdtBO.getLocations().get(0).setShelfPiece(SDUtil.convertToInt(pcsValue));

            if (caseValue.equals("-1") && pcsValue.equals("-1") && outerValue.equals("-1")) {
                orderBtn.setText("STOCK");
            } else {
                int val = (pdtBO.getLocations().get(0).getShelfCase() * pdtBO.getCaseSize())
                        + (pdtBO.getLocations().get(0).getShelfOuter() * pdtBO.getOutersize())
                        + pdtBO.getLocations().get(0).getShelfPiece();
                orderBtn.setText("Stock - " + val + "");
            }
        } else {
            if (selectedTextView != null) {
                updateTotalValue("SUBMIT");
                catalogOrderValueUpdate.updateTotalValue(pdtBO.getOrderedPcsQty() + "");
                //mSelectedTV.setText(value);
                if (String.valueOf(pdtBO.getOrderedCaseQty()).equals("0") && String.valueOf(pdtBO.getOrderedOuterQty()).equals("0")
                        && String.valueOf(pdtBO.getOrderedPcsQty()).equals("0")) {
                    orderBtn.setText(context.getResources().getString(R.string.order));
                } else {
                    orderBtn.setText(context.getResources().getString(R.string.ordered) + " - " + ((pdtBO.getOrderedCaseQty() * pdtBO.getCaseSize())
                            + (pdtBO.getOrderedOuterQty() * pdtBO.getOutersize())
                            + pdtBO.getOrderedPcsQty()) + "");
                }
                total_tv.setText(bmodel.formatValue(pdtBO.getTotalamount()) + "");
            }
        }

        isDialogCreated = false;

        dismiss();

    }

    private void addValue(String typedValue) {
        if (selectedTextView == null) {
            Toast.makeText(context, "Please Select an Item", Toast.LENGTH_LONG).show();
        } else {
            String value = selectedTextView.getText().toString();
            if (value.length() < limit) {
                if (typedValue.equals(".") && !value.contains(".") && value.length() < limit - 1) {
                    if (value.equals("0"))
                        value = "0.";
                    else
                        value = value + typedValue;
                } else if (!typedValue.equals(".")) {
                    if (value.equals("0")) {
                        value = typedValue;
                    } else {
                        value = value + typedValue;
                    }
                }

                if (selectedTextView.getId() == R.id.case_typed_value) {
                    case_typed_value.setText(value);
                } else if (selectedTextView.getId() == R.id.outer_case_typed_value) {
                    outer_case_typed_value.setText(value);
                } else if (selectedTextView.getId() == R.id.pcs_typed_value) {
                    pcs_typed_value.setText(value);
                }

                if (total_tv != null) {
                    updateTotalValue("ADD");
                } else {
                    calculateSONew();
                }
            }
        }

    }

    public void updateTotalValue(String isFrom) {
        String qty = selectedTextView.getText().toString();
        /** Calculate the total pcs qty **/
        float totalQty = 0;

        if (selectedTextView.getId() == R.id.case_typed_value) {
            totalQty = ((SDUtil.convertToInt(qty) * pdtBO.getCaseSize())
                    + (pdtBO.getOrderedPcsQty())
                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOutersize()));
        } else if (selectedTextView.getId() == R.id.outer_case_typed_value) {
            totalQty = ((pdtBO.getOrderedCaseQty() * pdtBO.getCaseSize())
                    + (pdtBO.getOrderedPcsQty())
                    + (SDUtil.convertToInt(qty)) * pdtBO.getOutersize());
        } else if (selectedTextView.getId() == R.id.pcs_typed_value) {
            totalQty = ((pdtBO.getOrderedCaseQty() * pdtBO.getCaseSize())
                    + (SDUtil.convertToInt(qty))
                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOutersize()));
        }

        //holder.weight.setText(totalQty * holder.productObj.getWeight() + "");

        if (pdtBO.isAllocation() == 1 && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (totalQty <= pdtBO.getSIH()) {
                if (!qty.equals("")) {
                    if (selectedTextView.getId() == R.id.case_typed_value) {
                        pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                    } else if (selectedTextView.getId() == R.id.outer_case_typed_value) {
                        pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                    } else if (selectedTextView.getId() == R.id.pcs_typed_value) {
                        pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                    }
                }
                double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                        + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                        + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
                value_keyboard.setText(context.getResources().getString(R.string.value) + ": " + bmodel.formatValue(tot));
                if (isFrom.equals("SUBMIT")) {
                    pdtBO.setTotalamount(tot);
                }

            } else {
                Toast.makeText(getContext(), String.format(getContext().getResources().getString(R.string.exceed),
                        pdtBO.getSIH()), Toast.LENGTH_SHORT).show();
                //Delete the last entered number and reset the qty
                qty = qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0";
                if (selectedTextView.getId() == R.id.case_typed_value) {
                    pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                } else if (selectedTextView.getId() == R.id.outer_case_typed_value) {
                    pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                } else if (selectedTextView.getId() == R.id.pcs_typed_value) {
                    pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                }
                //tv.setText(qty);
            }
        } else {
            if (!qty.equals("")) {
                if (selectedTextView.getId() == R.id.case_typed_value) {
                    pdtBO.setOrderedCaseQty(SDUtil.convertToInt(qty));
                } else if (selectedTextView.getId() == R.id.outer_case_typed_value) {
                    pdtBO.setOrderedOuterQty(SDUtil.convertToInt(qty));
                } else if (selectedTextView.getId() == R.id.pcs_typed_value) {
                    pdtBO.setOrderedPcsQty(SDUtil.convertToInt(qty));
                }
            }
            double tot = (pdtBO.getOrderedCaseQty() * pdtBO.getCsrp())
                    + (pdtBO.getOrderedPcsQty() * pdtBO.getSrp())
                    + (pdtBO.getOrderedOuterQty() * pdtBO.getOsrp());
            value_keyboard.setText(context.getResources().getString(R.string.value) + ": " + bmodel.formatValue(tot) + "");
            if (isFrom.equals("SUBMIT")) {
                pdtBO.setTotalamount(tot);
            }
        }
    }

    private int getTotalStockInPcs(ProductMasterBO product) {
        int totalQty = 0;
        Vector<StandardListBO> locationList = bmodel.productHelper
                .getInStoreLocation();

        int size = locationList.size();
        for (int i = 0; i < size; i++) {
            totalQty = totalQty
                    + product.getLocations().get(i).getShelfPiece()
                    + product.getLocations().get(i).getWHPiece()
                    + (product.getLocations().get(i).getShelfCase() * product
                    .getCaseSize())
                    + (product.getLocations().get(i).getWHCase() * product
                    .getCaseSize())
                    + (product.getLocations().get(i).getShelfOuter() * product
                    .getOutersize())
                    + (product.getLocations().get(i).getWHOuter() * product
                    .getOutersize());
        }
        return totalQty;

    }

    private void calculateSONew() {
        int SOLogic = bmodel.configurationMasterHelper.getSOLogic();
        int totalStockInPcs = getTotalStockInPcs(pdtBO);
        int so = 0;
        if (SOLogic == 1) {
            // Do nothing. Depreciated.
        } else if (SOLogic == 2) {
            so = pdtBO.getIco() - totalStockInPcs;
        } else if (SOLogic == 3) {
            so = pdtBO.getIco();
        } else if (SOLogic == 4) {
            int sellout = pdtBO.getIco() - totalStockInPcs;
            so = ((bmodel.userMasterHelper.getUserMasterBO().getUpliftFactor() * sellout) - totalStockInPcs)
                    * bmodel.userMasterHelper.getUserMasterBO()
                    .getSchemeFactor();
        }

        if (so < 0)
            so = 0;

        if (bmodel.configurationMasterHelper.SHOW_SO_SPLIT) {
            if (so < pdtBO.getCaseSize()
                    || so == 0
                    || pdtBO.getCaseSize() == 0) {

                pdtBO.setSoInventory(so);
                pdtBO.setSocInventory(0);
            } else if (so == pdtBO.getCaseSize()) {

                pdtBO.setSoInventory(0);
                pdtBO.setSocInventory(so / pdtBO.getCaseSize());
            } else {
                pdtBO.setSoInventory(so % pdtBO.getCaseSize());
                pdtBO.setSocInventory(so / pdtBO.getCaseSize());

            }
        } else {
            pdtBO.setSoInventory(so);
        }
        so_keyboard.setText("SO : " + pdtBO.getSoInventory());
    }

    private void setColor() {
        case_typed_value.setBackgroundColor(Color.TRANSPARENT);
        outer_case_typed_value.setBackgroundColor(Color.TRANSPARENT);
        pcs_typed_value.setBackgroundColor(Color.TRANSPARENT);
        selectedTextView.setBackgroundColor(getContext().getResources().getColor(R.color.drop_down_black));
    }

    private String getProductTotalValue() {
        int totalQty = 0;
        String totSTKQty = context.getResources().getString(R.string.stock) + ": ";

        if (bmodel.labelsMasterHelper.applyLabels(tv_totalStockQty.getTag()) != null)
            totSTKQty = bmodel.labelsMasterHelper
                    .applyLabels(tv_totalStockQty.getTag()) + ": ";
        Vector<StandardListBO> locationList = bmodel.productHelper
                .getInStoreLocation();

        int size = locationList.size();
        for (int i = 0; i < size; i++) {

            //Default value is -1 for stock fields, so adding only if value>0
            if (pdtBO.getLocations().get(i).getShelfPiece() > -1)
                totalQty += pdtBO.getLocations().get(i).getShelfPiece();

            if (pdtBO.getLocations().get(i).getShelfCase() > -1)
                totalQty += (pdtBO.getLocations().get(i).getShelfCase() * pdtBO
                        .getCaseSize());

            if (pdtBO.getLocations().get(i).getShelfOuter() > -1)
                totalQty += (pdtBO.getLocations().get(i).getShelfOuter() * pdtBO
                        .getOutersize());

            totalQty += pdtBO.getLocations().get(i).getWHPiece();
            totalQty += (pdtBO.getLocations().get(i).getWHCase() * pdtBO
                    .getCaseSize());
            totalQty += (pdtBO.getLocations().get(i).getWHOuter() * pdtBO
                    .getOutersize());
        }

        pdtBO.setTotalStockQty(totalQty);
        return (totSTKQty + String.valueOf(totalQty));
    }

    private boolean hasStockChecked() {
        int siz1 = pdtBO.getLocations().size();
        for (int j = 0; j < siz1; j++) {
            if (pdtBO.getLocations().get(j).getShelfPiece() > -1
                    || pdtBO.getLocations().get(j).getShelfCase() > -1
                    || pdtBO.getLocations().get(j).getShelfOuter() > -1)
                return true;
        }

        return false;
    }
}