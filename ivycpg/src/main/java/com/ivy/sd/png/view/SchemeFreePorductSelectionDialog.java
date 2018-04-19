package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.List;

public class SchemeFreePorductSelectionDialog extends Dialog {


    private static final String AND_LOGIC = "AND";
    private static final String ANY_LOGIC = "ANY";
    private static final String ONLY_LOGIC = "ONLY";


    private Context mContext;
    private BusinessModel mBmodel;

    private FreeProductSelectionListener mFreeProductListener;


    private SchemeBO mSchemeBO;
    private List<SchemeProductBO> mFreeProducts;
    private List<SchemeProductBO> mFreeProductsList;

    private ArrayList<String> mFreeGroupNameList;

    private EditText QUANTITY;
    private ListView mListView;

    private int[] mButtonIds = new int[]{R.id.calcone, R.id.calctwo,
            R.id.calcthree, R.id.calcfour, R.id.calcfive, R.id.calcsix,
            R.id.calcseven, R.id.calceight, R.id.calcnine, R.id.calczero,
            R.id.calcdot, R.id.calcdel};
    private Button[] mKeyPadButtons = new Button[mButtonIds.length];
    private SchemeProductBO mSelectedSchemeProductBO;
    private Toolbar toolbar;
    boolean isFromCounterSales=false;

    public SchemeFreePorductSelectionDialog(Context context,
                                            SchemeBO schemeBO,
                                            FreeProductSelectionListener freeProductListener,String screenCode) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mContext = context;
        mBmodel = (BusinessModel) mContext.getApplicationContext();

        if(screenCode.equalsIgnoreCase("CSale")) {
            this.isFromCounterSales = true;
        }

        mSchemeBO = schemeBO;
        mFreeProductListener = freeProductListener;
        mFreeProducts = mSchemeBO.getFreeProducts();
        mFreeProductsList = new ArrayList<>();
        for (SchemeProductBO schemeProductBO : mFreeProducts) {
            ProductMasterBO productBO = mBmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
            if (productBO != null) {
                mFreeProductsList.add(schemeProductBO);
            }
        }

        setContentView(R.layout.dialog_scheme_free_product_selection);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mSchemeBO.getScheme() + "");
        toolbar.setTitleTextColor(Color.WHITE);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        mTitle.setTypeface(mBmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        mListView = (ListView) findViewById(R.id.lv);

        if (mSchemeBO.getIsFreeCombination() == 1) {
            mFreeGroupNameList = mBmodel.schemeDetailsMasterHelper
                    .getFreeGroupNameListBySchemeID().get(
                            mSchemeBO.getSchemeId());
        }

		/*
         * if (mSchemeBO.getIsFreeCombination() == 0) { int freeQuantity =
		 * mSchemeBO.getActualQuantity();
		 * 
		 * if (mFreeProducts != null) { mFreeProductsForAdapter = new
		 * ArrayList<SchemeProductBO>();
		 * 
		 * ProductMasterBO productMasterBO; for (SchemeProductBO schemePdtBO :
		 * mFreeProducts) { if (schemePdtBO.getGroupBuyType().equals(AND_LOGIC))
		 * { freeQuantity = mSchemeBO.getActualQuantity(); }
		 * 
		 * productMasterBO = mBmodel.productHelper
		 * .getProductMasterBOById(schemePdtBO.getProductId());
		 * 
		 * int stock = productMasterBO.getSIH() -
		 * ((productMasterBO.getOrderedCaseQty() * productMasterBO
		 * .getCaseSize()) + (productMasterBO.getOrderedOuterQty() *
		 * productMasterBO .getOutersize()) + productMasterBO
		 * .getOrderedPcsQty());
		 * 
		 * schemePdtBO.setStock(stock);
		 * 
		 * if (mBmodel.configurationMasterHelper.IS_INVOICE) { if
		 * (context.getResources().getBoolean( R.bool.config_is_sih_considered))
		 * {
		 * 
		 * if (stock > 0) {
		 * 
		 * // if ((stock - freeQuantity) >= 0) { //
		 * schemePdtBO.setQuantitySelected(freeQuantity); //
		 * schemePdtBO.setQuantityTypeSelected(true); //
		 * schemePdtBO.setChecked(true); // freeQuantity = 0; // } else { //
		 * schemePdtBO.setQuantitySelected(stock); //
		 * schemePdtBO.setChecked(true); //
		 * schemePdtBO.setQuantityTypeSelected(true); // freeQuantity -= stock;
		 * // }
		 * 
		 * mFreeProductsForAdapter.add(schemePdtBO); } } else {
		 * schemePdtBO.setQuantitySelected(freeQuantity);
		 * schemePdtBO.setQuantityTypeSelected(true);
		 * schemePdtBO.setChecked(true);
		 * mFreeProductsForAdapter.add(schemePdtBO); } } else {
		 * schemePdtBO.setQuantitySelected(freeQuantity);
		 * schemePdtBO.setQuantityTypeSelected(true);
		 * schemePdtBO.setChecked(true);
		 * mFreeProductsForAdapter.add(schemePdtBO); } } } } else if
		 * (mSchemeBO.getIsFreeCombination() == 1) { mFreeProductsForAdapter =
		 * new ArrayList<SchemeProductBO>();
		 * 
		 * mFreeGroupNameList = mBmodel.schemeDetailsMasterHelper
		 * .getFreeGroupNameListBySchemeID().get( mSchemeBO.getSchemeId());
		 * if (mFreeGroupNameList != null) { Log.d(TAG, "Group Name list size :"
		 * + mFreeGroupNameList.size()); for (String freeGroupName :
		 * mFreeGroupNameList) {
		 * 
		 * if (mFreeProducts != null) {
		 * 
		 * ProductMasterBO productMasterBO; for (SchemeProductBO schemePdtBO :
		 * mFreeProducts) { if (freeGroupName
		 * .equals(schemePdtBO.getGroupName())) {
		 * 
		 * int freeQuantity = schemePdtBO .getQuantityActualCalculated();
		 * 
		 * productMasterBO = mBmodel.productHelper
		 * .getProductMasterBOById(schemePdtBO .getProductId());
		 * 
		 * int stock = productMasterBO.getSIH() -
		 * ((productMasterBO.getOrderedCaseQty() * productMasterBO
		 * .getCaseSize()) + (productMasterBO .getOrderedOuterQty() *
		 * productMasterBO .getOutersize()) + productMasterBO
		 * .getOrderedPcsQty());
		 * 
		 * schemePdtBO.setStock(stock);
		 * 
		 * 
		 * if (mBmodel.configurationMasterHelper.IS_INVOICE) { if (context
		 * .getResources() .getBoolean( R.bool.config_is_sih_considered)) {
		 * 
		 * if (stock > 0) {
		 * 
		 * // if ((stock - freeQuantity) >= // 0) { //
		 * schemePdtBO.setQuantitySelected(freeQuantity); //
		 * schemePdtBO.setQuantityTypeSelected(true); //
		 * schemePdtBO.setChecked(true); // freeQuantity = 0; // } else { //
		 * schemePdtBO.setQuantitySelected(stock); //
		 * schemePdtBO.setChecked(true); //
		 * schemePdtBO.setQuantityTypeSelected(true); // freeQuantity -= stock;
		 * // } schemePdtBO .setQuantitySelected(freeQuantity); schemePdtBO
		 * .setQuantityTypeSelected(true); schemePdtBO.setChecked(true);
		 * 
		 * mFreeProductsForAdapter .add(schemePdtBO); } } else { schemePdtBO
		 * .setQuantitySelected(freeQuantity); schemePdtBO
		 * .setQuantityTypeSelected(true); schemePdtBO.setChecked(true);
		 * mFreeProductsForAdapter .add(schemePdtBO); } } else { schemePdtBO
		 * .setQuantitySelected(freeQuantity); schemePdtBO
		 * .setQuantityTypeSelected(true); schemePdtBO.setChecked(true);
		 * mFreeProductsForAdapter .add(schemePdtBO); }
		 * 
		 * 
		 * }
		 * 
		 * } } }
		 * 
		 * } }
		 */
        mListView.setAdapter(new ProductAdapter());

        int size = mButtonIds.length;
        for (int i = 0; i < size; i++) {
            mKeyPadButtons[i] = (Button) findViewById(mButtonIds[i]);
            mKeyPadButtons[i].setOnClickListener(mKeyPadListener);
        }

        ((Button) findViewById(R.id.calcdot)).setVisibility(View.GONE);

        ((Button) findViewById(R.id.btn_apply))
                .setOnClickListener(mApplyListener);
        ((Button) findViewById(R.id.btn_cancel))
                .setOnClickListener(mCancelListener);
        ((Button) findViewById(R.id.btn_apply)).setTypeface(mBmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) findViewById(R.id.btn_cancel)).setTypeface(mBmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
    }

    class ProductAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public ProductAdapter() {
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            if (mFreeProductsList == null)
                return 0;
            return mFreeProductsList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            final FreeProductHolder holder;

            if (view == null) {
                holder = new FreeProductHolder();

                view = mInflater.inflate(R.layout.row_scheme_free_product,
                        parent, false);

                holder.productNameTV = (TextView) view
                        .findViewById(R.id.tv_name);
                holder.quantityET = (EditText) view
                        .findViewById(R.id.et_qty_pieces);

                holder.sihTV = (TextView) view.findViewById(R.id.tv_sih);
                holder.minValueTV = (TextView) view
                        .findViewById(R.id.tv_min_actualvalue);
                holder.maxValueTV = (TextView) view
                        .findViewById(R.id.tv_maxvalue);

                holder.groupTypeTV = (TextView) view
                        .findViewById(R.id.tv_group_type);


                holder.productNameTV.setTypeface(mBmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.quantityET.setTypeface(mBmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.sihTV.setTypeface(mBmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.minValueTV.setTypeface(mBmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.maxValueTV.setTypeface(mBmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.groupTypeTV.setTypeface(mBmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


                holder.productNameTV
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (mBmodel.schemeDetailsMasterHelper.IS_SCHEME_EDITABLE) {
                                    QUANTITY = holder.quantityET;
                                    QUANTITY.requestFocus();
                                    QUANTITY.setCursorVisible(false);
                                }
                            }
                        });

                holder.quantityET.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        int quantityEntered = 0;
                        String qty = s.toString();
                        if (s != null) {
                            if (!s.toString().trim().equals("")) {
                                quantityEntered = Integer.parseInt(s.toString());
                            }
                        }

						/*
                         * if (mBmodel.configurationMasterHelper.IS_INVOICE) {
						 * 
						 * if (quantityEntered > holder.schemeProductBO
						 * .getStock()) {
						 * holder.quantityET.removeTextChangedListener(this);
						 * holder.quantityET.setText("0");
						 * holder.quantityET.addTextChangedListener(this);
						 * showAlert("Quantity exceeds SIH"); return; } }
						 */

                        if (quantityEntered > holder.schemeProductBO
                                .getQuantityMaxiumCalculated()) {


                            holder.quantityET.removeTextChangedListener(this);
                            /**
                             * Delete the last entered number and reset the qty
                             **/
                            holder.quantityET.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                            holder.quantityET.addTextChangedListener(this);

                            Toast.makeText(
                                    mContext,

                                    mContext.getResources().getString(
                                            R.string.exceed_free_product),
                                    Toast.LENGTH_SHORT).show();
                        } else if (!isSchemeApplied(holder.schemeProductBO,
                                quantityEntered)) {
                            holder.quantityET.removeTextChangedListener(this);
                            /**
                             * Delete the last entered number and reset the qty
                             **/
                            holder.quantityET.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                            holder.quantityET.addTextChangedListener(this);

                            Toast.makeText(
                                    mContext,

                                    mContext.getResources().getString(
                                            R.string.exceed_free_product),
                                    Toast.LENGTH_SHORT).show();

							/*
                             * }else if ((quantityEntered + holder.schemeBO
							 * .getSelectedFreeProductsQuantity
							 * (holder.schemeProductBO .getProductId())) >
							 * mSchemeBO .getMaximumQuantity()) {
							 * 
							 * Logs.exception(TAG, "Entered : " +
							 * quantityEntered); Logs.exception( TAG,
							 * "Group Entered : " + holder.schemeBO
							 * .getSelectedFreeProductsQuantity
							 * (holder.schemeProductBO .getProductId()));
							 * Logs.exception( TAG, "Free Max : " +
							 * holder.schemeProductBO
							 * .getQuantityMaxiumCalculated());
							 * 
							 * Logs.debug(TAG, "Exceeds QTY* : " +
							 * quantityEntered);
							 * holder.quantityET.removeTextChangedListener
							 * (this);
							 *//**
                             * Delete the last entered number and reset the
                             * qty
                             **/
                            /*
                             * holder.quantityET.setText(qty.length() > 1 ? qty
							 * .substring(0, qty.length() - 1) : "0");
							 * holder.quantityET.addTextChangedListener(this);
							 * Toast.makeText( mContext,
							 * 
							 * mContext.getResources().getString(
							 * R.string.exceed_free_product),
							 * Toast.LENGTH_SHORT).show();
							 */
                        } else {
                            if (mBmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                ProductMasterBO productBO = mBmodel.productHelper.getProductMasterBOById(holder.schemeProductBO.getProductId());
                                int stock = 0;
                                if (productBO != null) {
                                    int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());

                                    // This condition is used if same product is ordered and also it is in scheme free product.
                                    //getting total stock available to give free
                                    if(isFromCounterSales){
                                        // For counter sales, if same product is in normal and free type then it will have unique product id. So no need to reduce
                                        stock = productBO.getCsFreeSIH();
                                    }
                                    else {
                                        //total ordered qty is reduced in SIH
                                        stock = productBO.getSIH() - totalQty;
                                    }

                                }

								if (quantityEntered <= stock) {
									holder.schemeProductBO
											.setQuantitySelected(quantityEntered);
								}
								else{
									holder.quantityET.removeTextChangedListener(this);
									Toast.makeText(
											mContext,
											String.format(
													mContext.getResources()
															.getString(
																	R.string.exceed),
													holder.schemeProductBO
															.getStock()),
											Toast.LENGTH_SHORT).show();
									/**
									 * Delete the last entered number and reset
									 * the qty
									 **/
									holder.quantityET.setText(qty.length() > 1 ? qty
											.substring(0, qty.length() - 1)
											: "0");
									holder.quantityET.addTextChangedListener(this);
								}

                            } else {

                                holder.schemeProductBO
                                        .setQuantitySelected(quantityEntered);

                            }
                        }

                    }

                });

                holder.quantityET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.quantityET;
                        int inType = holder.quantityET.getInputType();
                        holder.quantityET.setInputType(InputType.TYPE_NULL);
                        holder.quantityET.onTouchEvent(event);
                        holder.quantityET.setInputType(inType);
                        holder.quantityET.selectAll();
                        holder.quantityET.requestFocus();
                        QUANTITY.setCursorVisible(false);
                        return true;
                    }
                });

                view.setTag(holder);
            } else {
                holder = (FreeProductHolder) view.getTag();
            }


            holder.schemeBO = mSchemeBO;
            holder.schemeProductBO = mFreeProductsList.get(position);

            holder.productNameTV.setText(holder.schemeProductBO
                    .getProductName());

            holder.sihTV.setText("SIH : " + holder.schemeProductBO.getStock());
            holder.minValueTV
                    .setText("Min : "
                            + holder.schemeProductBO
                            .getQuantityActualCalculated() + "");
            holder.maxValueTV
                    .setText("Max : "
                            + holder.schemeProductBO
                            .getQuantityMaxiumCalculated() + "");
            if (mSchemeBO.getIsFreeCombination() == 1) {
                holder.groupTypeTV.setText(""
                        + holder.schemeProductBO.getGroupBuyType());
            } else {
                holder.groupTypeTV.setText(""
                        + mSchemeBO.getFreeType());
            }

            if (mBmodel.configurationMasterHelper.IS_INVOICE) {
                holder.sihTV.setVisibility(View.VISIBLE);
            } else {
                holder.sihTV.setVisibility(View.GONE);
            }

            if (!mBmodel.schemeDetailsMasterHelper.IS_SCHEME_EDITABLE) {
                holder.quantityET.setEnabled(false);
            } else {
                holder.quantityET.setEnabled(true);
            }
            holder.quantityET.setText(holder.schemeProductBO
                    .getQuantitySelected() + "");

            return view;
        }

    }

    private class FreeProductHolder {

        SchemeBO schemeBO;
        SchemeProductBO schemeProductBO;
        TextView productNameTV;
        TextView sihTV;
        EditText quantityET;
        TextView minValueTV;
        TextView maxValueTV;
        TextView groupTypeTV;
    }

    private View.OnClickListener mApplyListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mFreeProductsList != null) {

                if (isEnteredMinimumOffered()) {
                    if (mFreeProductListener != null) {
                        mFreeProductListener.onSelect();
                    }

                    dismiss();
                } else {
                    if (mSelectedSchemeProductBO != null) {
                        if (mSchemeBO.getIsFreeCombination() == 1) {
                            Toast.makeText(
                                    mContext,

                                    " Please enter minimum offered of "
                                            + mSelectedSchemeProductBO
                                            .getGroupName(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(
                                    mContext,

                                    " Please enter minimum offered of "
                                            + mSchemeBO.getScheme(),
                                    Toast.LENGTH_LONG).show();
                        }
                        mSelectedSchemeProductBO = null;
                    } else {
                        Toast.makeText(mContext,

                                " Please enter minimum offered of one scheme",
                                Toast.LENGTH_LONG).show();
                    }

                }
            } else {
                dismiss();
            }
        }
    };

    private View.OnClickListener mCancelListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mFreeProductListener != null) {
                mFreeProductListener.onCancel();
            }
            dismiss();
        }
    };

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    private boolean numPressed;
    private String append = "";

    private void showAlert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("Scheme Free Products");
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBmodel.applyAlertDialogTheme(dialog);
    }

    private View.OnClickListener mKeyPadListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            numPressed = true;

            if (QUANTITY == null) {
                showAlert(mContext.getResources().getString(
                        R.string.please_select_item));

            } else {
                int id = v.getId();
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
                    int s = SDUtil.convertToInt((String) QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(s + "");
                } else if (id == R.id.calcdot) {
                    append = ".";
                    eff();
                }
            }

        }
    };

    // public void numberPressed(View vw) {
    //
    // numPressed = true;
    //
    // if (QUANTITY == null) {
    // showAlert(mContext.getResources().getString(
    // R.string.Please_select_an_Item));
    //
    //
    // } else {
    // int id = vw.getId();
    // if (id == R.id.calcone) {
    // append = "1";
    // eff();
    // } else if (id == R.id.calctwo) {
    // append = "2";
    // eff();
    // } else if (id == R.id.calcthree) {
    // append = "3";
    // eff();
    // } else if (id == R.id.calcfour) {
    // append = "4";
    // eff();
    // } else if (id == R.id.calcfive) {
    // append = "5";
    // eff();
    // } else if (id == R.id.calcsix) {
    // append = "6";
    // eff();
    // } else if (id == R.id.calcseven) {
    // append = "7";
    // eff();
    // } else if (id == R.id.calceight) {
    // append = "8";
    // eff();
    // } else if (id == R.id.calcnine) {
    // append = "9";
    // eff();
    // } else if (id == R.id.calczero) {
    // append = "0";
    // eff();
    // } else if (id == R.id.calcdel) {
    // int s = SDUtil.convertToInt((String) QUANTITY.getText()
    // .toString());
    // s = s / 10;
    // QUANTITY.setText(s + "");
    // } else if (id == R.id.dot) {
    // append = ".";
    // eff();
    // }
    // }
    //
    // }

    /**
     * Method to check,All entered quantity value is equal or greater than of
     * minimum value entered
     *
     * @return true - entered quantity >= minimum offered qty
     */

    private boolean isEnteredMinimumOffered() {
        if (mSchemeBO.getIsFreeCombination() == 1) {
            if (mSchemeBO.isSihAvailableForFreeProducts()) {
                if (mSchemeBO.getFreeType().equals(AND_LOGIC) || mSchemeBO.getFreeType().equals(ONLY_LOGIC)) {
                    if (mFreeGroupNameList != null) {
                        for (String groupName : mFreeGroupNameList) {

                            int totalFreeQty = 0;
                            int anyLogicMinimumCount = 0;
                            for (SchemeProductBO schemeProductBo : mFreeProductsList) {
                                if (groupName
                                        .equals(schemeProductBo.getGroupName())) {
                                    if (schemeProductBo.getGroupBuyType().equals(
                                            AND_LOGIC) || schemeProductBo.getGroupBuyType().equals(ONLY_LOGIC)) {

                                        if (schemeProductBo.getQuantitySelected() < schemeProductBo
                                                .getQuantityActualCalculated()) {
                                            mSelectedSchemeProductBO = schemeProductBo;
                                            return false;
                                        }

                                    } else if (schemeProductBo.getGroupBuyType()
                                            .equals(ANY_LOGIC)) {
                                        totalFreeQty = totalFreeQty
                                                + schemeProductBo
                                                .getQuantitySelected();
                                        mSelectedSchemeProductBO = schemeProductBo;
                                        if (totalFreeQty >= schemeProductBo
                                                .getQuantityActualCalculated()) {
                                            anyLogicMinimumCount = anyLogicMinimumCount + 1;

                                        }

                                    }
                                }

                            }
                            if (mBmodel.schemeDetailsMasterHelper.getGroupBuyTypeByGroupName().get(mSchemeBO.getSchemeId() + groupName) != null) {
                                if (mBmodel.schemeDetailsMasterHelper
                                        .getGroupBuyTypeByGroupName().get(mSchemeBO.getSchemeId() + groupName)
                                        .equals(ANY_LOGIC)) {
                                    if (anyLogicMinimumCount == 0) {
                                        return false;
                                    }
                                }
                            }

                        }

                    }
                } else if (mSchemeBO.getFreeType().equals(ANY_LOGIC)) {
                    String type = isAlreadyEnteredSchemeGroupName();
                    if (!type.equals("")) {

                        int totalFreeQty = 0;
                        int anyLogicMinimumCount = 0;
                        for (SchemeProductBO schemeProductBo : mFreeProductsList) {
                            if (type.equals(schemeProductBo.getGroupName())) {
                                if (schemeProductBo.getGroupBuyType().equals(
                                        AND_LOGIC) || schemeProductBo.getGroupBuyType().equals(ONLY_LOGIC)) {

                                    if (schemeProductBo.getQuantitySelected() < schemeProductBo
                                            .getQuantityActualCalculated()) {
                                        mSelectedSchemeProductBO = schemeProductBo;
                                        return false;
                                    }

                                } else if (schemeProductBo.getGroupBuyType()
                                        .equals(ANY_LOGIC)) {
                                    totalFreeQty = totalFreeQty
                                            + schemeProductBo.getQuantitySelected();
                                    mSelectedSchemeProductBO = schemeProductBo;
                                    if (totalFreeQty >= schemeProductBo
                                            .getQuantityActualCalculated()) {
                                        anyLogicMinimumCount = anyLogicMinimumCount + 1;

                                    }

                                }
                            }

                        }
                        if (mBmodel.schemeDetailsMasterHelper
                                .getGroupBuyTypeByGroupName().get(mSchemeBO.getSchemeId() + type)
                                .equals(ANY_LOGIC)) {
                            if (anyLogicMinimumCount == 0) {
                                return false;
                            }
                        }

                    } else {
                        return false;
                    }

                }
            }


        } else if (mSchemeBO.getIsFreeCombination() == 0) {
            int totalFreeQty = 0;
            int count = 0;
            for (SchemeProductBO schemeProductBo : mFreeProductsList) {
                count = count + 1;
                mSelectedSchemeProductBO = schemeProductBo;

                if (mSchemeBO.getFreeType().equals(AND_LOGIC) || mSchemeBO.getFreeType().equals(ONLY_LOGIC)) {
                    if (schemeProductBo.getQuantitySelected() < schemeProductBo
                            .getQuantityActualCalculated()) {
                        return false;
                    }

                } else if (mSchemeBO.getFreeType().equals(ANY_LOGIC)) {

                    totalFreeQty = totalFreeQty
                            + schemeProductBo.getQuantitySelected();

                    if (count == mFreeProductsList.size()) {
                        if (totalFreeQty < schemeProductBo
                                .getQuantityActualCalculated()) {
                            return false;
                        }
                    }

                }
            }

        }


        return true;

		/*
         * int totalEnteredQty = 0; for (SchemeProductBO schemeProductBo :
		 * mFreeProductsForAdapter) { totalEnteredQty = totalEnteredQty +
		 * schemeProductBo.getQuantitySelected();
		 * 
		 * }
		 * 
		 * if (totalEnteredQty < mSchemeBO.getActualQuantity()) { return false;
		 * } else { return true; }
		 */
    }

    public interface FreeProductSelectionListener {
        public void onSelect();

        public void onCancel();
    }

    /**
     * Method to check and validate enteredvalue,which is not greater than
     * scheme maximum value
     *
     * @param schemeProductBo
     * @param qtyEntered
     * @return
     */

    private boolean isSchemeApplied(SchemeProductBO schemeProductBo,
                                    int qtyEntered) {
        if (mSchemeBO.getIsFreeCombination() == 1) {
            if (mSchemeBO.getFreeType().equals(AND_LOGIC) || mSchemeBO.getFreeType().equals(ONLY_LOGIC)) {
                if (schemeProductBo.getGroupBuyType().equals(AND_LOGIC) || schemeProductBo.getGroupBuyType().equals(ONLY_LOGIC)) {
                    if (qtyEntered > schemeProductBo
                            .getQuantityMaxiumCalculated()) {
                        return false;
                    }

                } else if (schemeProductBo.getGroupBuyType().equals(ANY_LOGIC)) {

                    int totalFreeQty = qtyEntered;
                    for (SchemeProductBO schemePrtBO : mFreeProductsList) {
                        if (schemeProductBo.getGroupName().equals(
                                schemePrtBO.getGroupName())) {

                            if (!schemeProductBo.getProductId().equals(
                                    schemePrtBO.getProductId())) {
                                totalFreeQty = totalFreeQty
                                        + schemePrtBO.getQuantitySelected();
                            }

                        }
                    }

                    if (totalFreeQty > schemeProductBo
                            .getQuantityMaxiumCalculated()) {
                        return false;
                    }
                }
            } else if (mSchemeBO.getFreeType().equals(ANY_LOGIC)) {
                if (!isAlreadyEnteredOtherChildScheme(schemeProductBo)) {

                    if (schemeProductBo.getGroupBuyType().equals(AND_LOGIC) || schemeProductBo.getGroupBuyType().equals(ONLY_LOGIC)) {
                        if (qtyEntered > schemeProductBo
                                .getQuantityMaxiumCalculated()) {
                            return false;
                        }
                    } else if (schemeProductBo.getGroupBuyType().equals(
                            ANY_LOGIC)) {
                        int totalFreeQty = qtyEntered;
                        for (SchemeProductBO schemePrtBO : mFreeProductsList) {
                            if (schemeProductBo.getGroupName().equals(
                                    schemePrtBO.getGroupName())) {

                                if (!schemeProductBo.getProductId().equals(
                                        schemePrtBO.getProductId())) {
                                    totalFreeQty = totalFreeQty
                                            + schemePrtBO.getQuantitySelected();
                                }

                            }
                        }

                        if (totalFreeQty > schemeProductBo
                                .getQuantityMaxiumCalculated()) {
                            return false;
                        }

                    }

                } else {
                    if (qtyEntered > 0) {
                        return false;
                    }
                }
            }

        } else if (mSchemeBO.getIsFreeCombination() == 0) {
            if (mSchemeBO.getFreeType().equals(AND_LOGIC)) {
                if (qtyEntered > schemeProductBo
                        .getQuantityMaxiumCalculated()) {
                    return false;
                }
            } else if (mSchemeBO.getFreeType().equals(ANY_LOGIC)) {
                int totalFreeQty = qtyEntered;
                for (SchemeProductBO schemePrtBO : mFreeProductsList) {
                    if (!schemeProductBo.getProductId().equals(
                            schemePrtBO.getProductId())) {
                        totalFreeQty = totalFreeQty
                                + schemePrtBO.getQuantitySelected();
                    }

                }
                if (totalFreeQty > schemeProductBo
                        .getQuantityMaxiumCalculated()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAlreadyEnteredOtherChildScheme(SchemeProductBO schemePrtBo) {
        for (SchemeProductBO schemeProductBO : mFreeProductsList) {
            if (!schemePrtBo.getGroupName().equals(
                    schemeProductBO.getGroupName())) {
                if (schemeProductBO.getQuantitySelected() > 0) {
                    return true;
                }
            }
        }
        return false;

    }

    private String isAlreadyEnteredSchemeGroupName() {
        for (SchemeProductBO schemeProductBO : mFreeProductsList) {
            if (schemeProductBO.getQuantitySelected() > 0) {
                return schemeProductBO.getGroupName();
            }
        }

        return "";
    }

}
