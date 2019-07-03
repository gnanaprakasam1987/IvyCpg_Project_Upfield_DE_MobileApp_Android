package com.ivy.cpg.view.order.scheme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.List;

public class SchemeFreeProductSelectionDialog extends Dialog implements View.OnClickListener {


    private Context mContext;
    private BusinessModel bModel;

    private FreeProductSelectionListener mFreeProductListener;


    private SchemeBO mSchemeBO;
    private List<SchemeProductBO> mFreeProductsList;


    private EditText QUANTITY;

    private String groupName="";



    private SchemeDetailsMasterHelper schemeHelper;

    public SchemeFreeProductSelectionDialog(Context context,
                                            SchemeBO schemeBO,
                                            FreeProductSelectionListener freeProductListener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(getWindow()!=null)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mContext = context;
        bModel = (BusinessModel) mContext.getApplicationContext();
        mSchemeBO=schemeBO;
        schemeHelper=SchemeDetailsMasterHelper.getInstance(context);


        setContentView(R.layout.dialog_scheme_free_product_selection);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mSchemeBO.getScheme());
        toolbar.setTitleTextColor(Color.WHITE);
        TextView mTitle =  toolbar.findViewById(R.id.tv_toolbar_title);
        mTitle.setTypeface(bModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

       // toolbar.setSubtitle(schemeBO.getFreeType());

        int[] mButtonIds = new int[]{R.id.calcone, R.id.calctwo,
                R.id.calcthree, R.id.calcfour, R.id.calcfive, R.id.calcsix,
                R.id.calcseven, R.id.calceight, R.id.calcnine, R.id.calczero,
                R.id.calcdot, R.id.calcdel};
        Button[] mKeyPadButtons = new Button[mButtonIds.length];
        int size = mButtonIds.length;
        for (int i = 0; i < size; i++) {
            mKeyPadButtons[i] =  findViewById(mButtonIds[i]);
            mKeyPadButtons[i].setOnClickListener(mKeyPadListener);
        }

        (findViewById(R.id.calcdot)).setVisibility(View.GONE);
        (findViewById(R.id.btn_apply)).setOnClickListener(this);
        (findViewById(R.id.btn_cancel)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_apply)).setTypeface(bModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) findViewById(R.id.btn_cancel)).setTypeface(bModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ListView mListView =  findViewById(R.id.lv);


        mFreeProductListener = freeProductListener;
        mFreeProductsList = new ArrayList<>();
        for (SchemeProductBO schemeProductBO : mSchemeBO.getFreeProducts()) {
            ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
            if (productBO != null) {
                mFreeProductsList.add(schemeProductBO);
            }
        }

        mListView.setAdapter(new ProductAdapter());

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

                holder.productNameTV =  view
                        .findViewById(R.id.tv_name);
                holder.quantityET =  view
                        .findViewById(R.id.et_qty_pieces);

                holder.sihTV =  view.findViewById(R.id.tv_sih);
                holder.minValueTV =  view
                        .findViewById(R.id.tv_min_actualvalue);
                holder.maxValueTV =  view
                        .findViewById(R.id.tv_maxvalue);

                holder.card_group=view.findViewById(R.id.card_group);

                holder.layout_group=view.findViewById(R.id.layout_group);
                holder.text_groupName=view.findViewById(R.id.text_group);


                holder.productNameTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.quantityET.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.sihTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.minValueTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.maxValueTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

                holder.text_groupName.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));



                holder.productNameTV
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (schemeHelper.IS_SCHEME_EDITABLE) {
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
                        if (qty.length() > 0)
                            holder.quantityET.setSelection(qty.length());
                            if (!s.toString().trim().equals("")) {
                                quantityEntered = SDUtil.convertToInt(s.toString());
                            }

                        if (quantityEntered > holder.schemeProductBO
                                .getQuantityMaxiumCalculated()) {


                            holder.quantityET.removeTextChangedListener(this);

                             //Delete the last entered number and reset the qty

                            holder.quantityET.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                            holder.quantityET.addTextChangedListener(this);

                            Toast.makeText(
                                    mContext,

                                    mContext.getResources().getString(
                                            R.string.exceed_free_product),
                                    Toast.LENGTH_SHORT).show();
                        } else if (schemeHelper.isEnteredQuantityExceedsMaximumOffered(mSchemeBO,holder.schemeProductBO,
                                quantityEntered,mFreeProductsList)) {
                            holder.quantityET.removeTextChangedListener(this);

                             // Delete the last entered number and reset the qty

                            holder.quantityET.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                            holder.quantityET.addTextChangedListener(this);

                            Toast.makeText(
                                    mContext,

                                    mContext.getResources().getString(
                                            R.string.exceed_free_product),
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            if (bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(holder.schemeProductBO.getProductId());
                                int stock = 0;
                                if (productBO != null) {
                                    int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());

                                    // This condition is used if same product is ordered and also it is in scheme free product.
                                    //getting total stock available to give free
                                    //total ordered qty is reduced in SIH
                                    if(bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE){
                                        stock = productBO.getFreeSIH() - totalQty;
                                    }
                                    else {
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
									//Delete the last entered number and reset the qty


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
                        holder.quantityET.requestFocus();
                        if (holder.quantityET.getText().length() > 0)
                            holder.quantityET.setSelection(holder.quantityET.getText().length());
                        QUANTITY.setCursorVisible(false);
                        InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(
                                holder.quantityET.getWindowToken(), 0);
                        return true;
                    }
                });

                view.setTag(holder);
            } else {
                holder = (FreeProductHolder) view.getTag();
            }

            holder.schemeBO = mSchemeBO;
            holder.schemeProductBO = mFreeProductsList.get(position);

            if(groupName.equals("")||!groupName.equals(holder.schemeProductBO.getGroupName())){
                holder.card_group.setVisibility(View.VISIBLE);

                if(!holder.schemeProductBO.getGroupLogic().equals(schemeHelper.ONLY_LOGIC))
                  holder.text_groupName.setText(holder.schemeProductBO.getGroupName()+" ("+holder.schemeProductBO.getGroupLogic()+")");
                else holder.text_groupName.setText(holder.schemeProductBO.getGroupName());


                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                if(position>0) {

                    Resources r = mContext.getResources();
                    int px = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            10,
                            r.getDisplayMetrics()
                    );
                    params.setMargins(0, px, 0, 0);
                    holder.card_group.setLayoutParams(params);
                }
                else {
                    params.setMargins(0, 0, 0, 0);
                    holder.card_group.setLayoutParams(params);
                }
            }
            else {
                holder.card_group.setVisibility(View.GONE);
            }
            groupName=holder.schemeProductBO.getGroupName();


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


            if ( bModel.getRetailerMasterBO().getIsVansales()==1
                    ||bModel.configurationMasterHelper.IS_INVOICE) {
                holder.sihTV.setVisibility(View.VISIBLE);
            } else {
                holder.sihTV.setVisibility(View.GONE);
            }

            if (!schemeHelper.IS_SCHEME_EDITABLE) {
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
        RelativeLayout layout_group;
        TextView text_groupName;
        CardView card_group;
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_apply){

            if (mFreeProductsList != null) {

                String inValidGroup=schemeHelper.isEnteredMinimumOffered(mSchemeBO,mFreeProductsList);
                if(!inValidGroup.equals("0")) {

                    if (inValidGroup.equals(""))
                        Toast.makeText(mContext, " Please enter minimum offered.", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(mContext, " Please enter minimum offered of " + inValidGroup, Toast.LENGTH_LONG).show();
                    return;
                }

                if (mFreeProductListener != null) {
                    mFreeProductListener.onSelect();
                }

                dismiss();

            } else {
                dismiss();
            }

        }
        else if(R.id.btn_cancel==view.getId()){
            if (mFreeProductListener != null) {
                mFreeProductListener.onCancel();
            }
            dismiss();
        }

    }


    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

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
        bModel.applyAlertDialogTheme(dialog);
    }

    private View.OnClickListener mKeyPadListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

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
                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(String.valueOf(s));
                } else if (id == R.id.calcdot) {
                    append = ".";
                    eff();
                }
            }

        }
    };




    public interface FreeProductSelectionListener {
       void onSelect();
         void onCancel();
    }



}
