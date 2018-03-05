package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
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
import com.ivy.sd.png.model.CatalogOrderValueUpdate;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.Vector;

/**
 * Created by subramanian.r on 23-03-2016.
 */
public class CustomKeyBoardCatalog extends Dialog implements View.OnClickListener {

    private TextView tv_value;

    private Button number_one, number_two, number_three;
    private Button number_four, number_five, number_six;
    private Button number_seven, number_eight, number_nine;
    private Button number_zero, decimal_point;

    private Button btn_cancel, btn_ok;
    private ImageButton btn_delete;
    private String value = "0";

    private TextView value_keyboard, total_tv, so_keyboard;
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


    public CustomKeyBoardCatalog(Context context, TextView total_tv, Button orderBtn, ProductMasterBO pdtBO, BusinessModel bmodel, boolean isDecimalAllowed) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_keyboard_catalog);
        TextView pdtNameTV = (TextView) findViewById(R.id.pdt_name_keyboard);
        TextView SIHTV = (TextView) findViewById(R.id.sih_keyboard);
        LinearLayout cust_layout = (LinearLayout) findViewById(R.id.cust_layout);

        TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        this.context = context;
        this.catalogOrderValueUpdate = (CatalogOrderValueUpdate) context;
        this.pdtBO = pdtBO;
        this.bmodel = bmodel;
        this.value_keyboard = (TextView) findViewById(R.id.value_keyboard);
        this.so_keyboard = (TextView) findViewById(R.id.so_keyboard);
        this.total_tv = total_tv;
        this.orderBtn = orderBtn;

        cust_layout.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
        pdtNameTV.setText(pdtBO.getProductName());
        SIHTV.setText("SIH : " + pdtBO.getSIH());
        so_keyboard.setText("SO : " + pdtBO.getSoInventory());

        isDialogCreated = true;

        if (total_tv == null) {
            value_keyboard.setVisibility(View.GONE);
            setKeyboard(((pdtBO.getLocations().get(0).getShelfPiece() != -1) ? pdtBO.getLocations().get(0).getShelfPiece() : 0) + "");
        } else {
            value_keyboard.setText("Value : " + bmodel.formatValue(pdtBO.getTotalamount()));
            //mSelectedTV = tv;
            setKeyboard(pdtBO.getOrderedPcsQty() + "");
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
    }


    private void setKeyboard(String s) {
        if (s.length() > 0)
            this.value = s;

        if (s.equals("-1"))
            value = "0";

        tv_value = (TextView) findViewById(R.id.typed_value);
        tv_value.setText(value);

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
        if (total_tv != null) {
            tv_value.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String qty = s.toString();
                    if (pdtBO.isAllocation() == 1
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if (SDUtil.convertToInt(s.toString()) <= pdtBO.getSIH()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }
                            double tot = (pdtBO.getOrderedPcsQty() * pdtBO
                                    .getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(context,
                                        String.format(
                                                context.getResources().getString(
                                                        R.string.exceed),
                                                pdtBO.getSIH()),
                                        Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                pdtBO.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                    } else if (pdtBO.isCbsihAvailable()) {
                        if (SDUtil.convertToInt(s.toString()) <= pdtBO.getCpsih()) {
                            isOrderAllowed = true;
                            if (!qty.equals("")) {
                                pdtBO.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (pdtBO.getOrderedPcsQty() * pdtBO
                                    .getSrp());
                            pdtBO.setTotalamount(tot);
                        } else {
                            isOrderAllowed = false;
                            if (!qty.equals("0")) {
                                Toast.makeText(
                                        context,
                                        String.format(
                                                context.getResources().getString(
                                                        R.string.exceed),
                                                pdtBO.getCpsih()),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";


                                pdtBO.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                    } else {
                        isOrderAllowed = true;
                        if (!qty.equals("")) {
                            pdtBO.setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                        }
                        double tot = (pdtBO.getOrderedPcsQty() * pdtBO
                                .getSrp());
                        pdtBO.setTotalamount(tot);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ok) {
            //update value

            if (value.endsWith("."))
                value = value.substring(0, value.length() - 1);
            if (total_tv == null) {
                //mSelectedView.setText(value);
                calculateSONew();
                if (value.equals("-1"))
                    pdtBO.getLocations()
                            .get(0).setShelfPiece(-1);
                else
                    pdtBO.getLocations()
                            .get(0).setShelfPiece(Integer.parseInt(value));
                if (value.equals("-1")) {
                    orderBtn.setText("STOCK");
                } else {
                    orderBtn.setText("Stock - " + value + "");
                }
            } else {
                updateTotalValue("SUBMIT");
                catalogOrderValueUpdate.updateTotalValue(pdtBO.getOrderedPcsQty() + "");
                //mSelectedTV.setText(value);
                if (String.valueOf(pdtBO.getOrderedPcsQty()).equals("0")) {
                    orderBtn.setText("ORDER");
                } else {
                    orderBtn.setText("Ordered - " + pdtBO.getOrderedPcsQty() + "");
                }
                total_tv.setText(bmodel.formatValue(pdtBO.getTotalamount()) + "");
            }

            isDialogCreated = false;
            dismiss();


        } else if (id == R.id.cancel) {
            dismiss();
            isDialogCreated = false;
            if (total_tv != null && total_tv.getText().toString().equals("0")) {
                catalogOrderValueUpdate.updateTotalValue(pdtBO.getOrderedPcsQty() + "");
            }
        } else if (id == R.id.delete) {
            value = value.substring(0, value.length() - 1);
            if (value.equals(""))
                value = "0";
            tv_value.setText(value);
            if (total_tv != null) {
                updateTotalValue("DEL");
            }
        } else {
            if (isOrderAllowed) {
                addValue(v.getTag().toString());
            }

        }
    }

    private void addValue(String typedValue) {
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

            tv_value.setText(value);
            if (total_tv != null) {
                updateTotalValue("ADD");
            } else {
                calculateSONew();
            }
        }

    }

    public void updateTotalValue(String isFrom) {
        String qty = value;
        /** Calculate the total pcs qty **/
        float totalQty = ((pdtBO.getOrderedCaseQty() * pdtBO
                .getCaseSize())
                + (SDUtil.convertToInt(qty))
                + (pdtBO.getOrderedOuterQty() * pdtBO
                .getOutersize()));

        //holder.weight.setText(totalQty * holder.productObj.getWeight() + "");

        if (pdtBO.isAllocation() == 1
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (totalQty <= pdtBO.getSIH()) {
                if (!qty.equals("")) {
                    pdtBO.setOrderedPcsQty(SDUtil
                            .convertToInt(qty));
                }
                double tot = (pdtBO
                        .getOrderedCaseQty() * pdtBO
                        .getCsrp())
                        + (pdtBO.getOrderedPcsQty() * pdtBO
                        .getSrp())
                        + (pdtBO
                        .getOrderedOuterQty() * pdtBO
                        .getOsrp());
                value_keyboard.setText("Value : " + bmodel.formatValue(tot));
                if (isFrom.equals("SUBMIT")) {
                    pdtBO.setTotalamount(tot);
                }

            } else {
                Toast.makeText(
                        getContext(),
                        String.format(
                                getContext().getResources().getString(
                                        R.string.exceed),
                                pdtBO.getSIH()),
                        Toast.LENGTH_SHORT).show();
                //Delete the last entered number and reset the qty
                qty = qty.length() > 1 ? qty.substring(0,
                        qty.length() - 1) : "0";
                pdtBO.setOrderedPcsQty(SDUtil
                        .convertToInt(qty));
                //tv.setText(qty);
            }
        } else {
            if (!qty.equals("")) {
                pdtBO.setOrderedPcsQty(SDUtil
                        .convertToInt(qty));
            }
            double tot = (pdtBO.getOrderedCaseQty() * pdtBO
                    .getCsrp())
                    + (pdtBO.getOrderedPcsQty() * pdtBO
                    .getSrp())
                    + (pdtBO.getOrderedOuterQty() * pdtBO
                    .getOsrp());
            value_keyboard.setText("Value : " + bmodel.formatValue(tot) + "");
            if (isFrom.equals("SUBMIT")) {
                pdtBO.setTotalamount(tot);
            }
        }
    }


    private int getProductTotalValue(ProductMasterBO product) {
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
        int totalStockInPcs = getProductTotalValue(pdtBO);
        int so = 0;
        if (SOLogic == 1) {
            so = bmodel.productHelper.calculateSO(pdtBO.getIco(),
                    totalStockInPcs, pdtBO.isRPS(),
                    pdtBO.getIsInitiativeProduct(),
                    pdtBO.getDropQty(), pdtBO.getInitDropSize());
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

}
