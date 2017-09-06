package com.ivy.countersales;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CsProductSchemeDetailsActivity;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 6/13/2017.
 */

public class CSchildProductsDialog extends Dialog {

    Context mContext;
    BusinessModel bmodel;
    ListView listView;
    ArrayList<ProductMasterBO> lstProducts;
    EditText QUANTITY;
    Button btnDone;
    private String append = "";
    private boolean numPressed;
    private int mTotalScreenWidth;
    Toolbar toolbar;
    private int[] mButtonIds = new int[]{R.id.calcone, R.id.calctwo,
            R.id.calcthree, R.id.calcfour, R.id.calcfive, R.id.calcsix,
            R.id.calcseven, R.id.calceight, R.id.calcnine, R.id.calczero,
            R.id.calcdot, R.id.calcdel};
    private Button[] mKeyPadButtons = new Button[mButtonIds.length];
    ProductGroupInterface mGroupinterface;

    public static final int PRODUCT_TYPE_NORMAL = 1;
    public static final int PRODUCT_TYPE_FREE = 2;
    public static final int PRODUCT_TYPE_ACCESSORIES = 3;
    int produuctType;
    String parentId;

    public CSchildProductsDialog(Context context, String productId, String productName, int prodType) {

        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mContext = context;
        bmodel = (BusinessModel) mContext.getApplicationContext();

        setContentView(R.layout.dialog_cs_child_products);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mGroupinterface = (ProductGroupInterface) context;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(productName);
        toolbar.setTitleTextColor(Color.WHITE);

        produuctType = prodType;
        this.parentId=productId;

        listView = (ListView) findViewById(R.id.lv);
        btnDone = (Button) findViewById(R.id.btn_done);


        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.pcsTitle).getTag()));
            } catch (Exception e) {
                Commons.print("" + e);
            }
        }


        for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {
            if (bo.isChildProduct()&&bo.getParentid()==Integer.parseInt(parentId)) {
                if (lstProducts == null)
                    lstProducts = new ArrayList<>();

                if (produuctType == PRODUCT_TYPE_FREE && bo.getMRP() == 0 && bo.getIsSaleable() == 1) {
                    lstProducts.add(bo);
                } else if (produuctType == PRODUCT_TYPE_NORMAL && bo.getMRP() != 0 && bo.getIsSaleable() == 1) {
                    lstProducts.add(bo);
                } else if (produuctType == PRODUCT_TYPE_ACCESSORIES && bo.getIsSaleable() == 0 && bo.getIsReturnable() == 0) {
                    lstProducts.add(bo);
                }

            }
        }

        if (produuctType == PRODUCT_TYPE_ACCESSORIES || produuctType == PRODUCT_TYPE_NORMAL) {
            findViewById(R.id.freePcsTitle).setVisibility(View.GONE);
            findViewById(R.id.free_sihTitle).setVisibility(View.GONE);
        } else if (prodType == PRODUCT_TYPE_FREE) {
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
            findViewById(R.id.sihTitle).setVisibility(View.GONE);


        }


        listView.setAdapter(new ProductAdapter());

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProductMasterBO parentBO=bmodel.productHelper.getProductMasterBOById(parentId);
                parentBO.setCsPiece(0);
                parentBO.setOrderedPcsQty(0);
                parentBO.setCsTotal(0);
                parentBO.setCsFreePiece(0);
                parentBO.setCsFreeTotal(0);
                for (ProductMasterBO bo : lstProducts) {

                    int total = bo.getCsPiece() + (bo.getCsCase() * bo.getCaseSize()) + (bo.getCsOuter() * bo.getOutersize());
                    parentBO.setCsPiece(parentBO.getCsPiece() + total);// cs piece re used for showing total qty
                    //parentBO.setOrderedPcsQty(masterBo.getCsPiece());
                    parentBO.setCsTotal((int) (parentBO.getCsTotal() + bo.getCsTotal()));//total value..

                    int freeTotal = bo.getCsFreePiece();
                    parentBO.setCsFreePiece(parentBO.getCsFreePiece() + freeTotal);
                    parentBO.setCsFreeTotal((int) (parentBO.getCsFreeTotal() + bo.getCsFreeTotal()));


                }

                mGroupinterface.onDismiss();
                dismiss();
            }
        });

        int size = mButtonIds.length;
        for (int i = 0; i < size; i++) {
            mKeyPadButtons[i] = (Button) findViewById(mButtonIds[i]);
            mKeyPadButtons[i].setOnClickListener(mKeyPadListener);
        }

        ((Button) findViewById(R.id.calcdot)).setVisibility(View.GONE);

        ((Button) findViewById(R.id.btn_done)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;
    }

    class ProductAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public ProductAdapter() {
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            if (lstProducts == null)
                return 0;
            return lstProducts.size();
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
        public View getView(int position, View row, ViewGroup parent) {

            final ViewHolder holder;
            final ProductMasterBO counterBo = lstProducts.get(position);
            if (row == null) {
                holder = new ViewHolder();
                row = mInflater.inflate(R.layout.row_cs_child_products,
                        parent, false);


                holder.psname = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_productname);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.pcsQty = (EditText) row
                        .findViewById(R.id.stock_and_order_listview_pcs_qty);


                holder.txt_sih = (TextView) row
                        .findViewById(R.id.txt_sih);
                holder.tv_barcode = (TextView) row
                        .findViewById(R.id.tv_barcode);

                holder.tv_barcode.setVisibility(View.GONE);
                holder.freePcs = (EditText) row
                        .findViewById(R.id.tv_free_pcs);

                holder.txt_free_sih = (TextView) row
                        .findViewById(R.id.txt_free_sih);

                holder.txt_mrp = (TextView) row
                        .findViewById(R.id.txt_mrp);
                holder.txt_total = (TextView) row
                        .findViewById(R.id.txt_total);
                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                holder.txt_sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tv_barcode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.freePcs.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.txt_free_sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.txt_mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.txt_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


                if (produuctType == PRODUCT_TYPE_ACCESSORIES || produuctType == PRODUCT_TYPE_NORMAL) {

                    holder.freePcs.setVisibility(View.GONE);
                    holder.txt_free_sih.setVisibility(View.GONE);
                } else if (produuctType == PRODUCT_TYPE_FREE) {
                    holder.pcsQty.setVisibility(View.GONE);
                    holder.txt_sih.setVisibility(View.GONE);

                }

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.pcsQty.setVisibility(View.GONE);


                holder.pcsQty
                        .setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                QUANTITY = holder.pcsQty;
                                QUANTITY.setTag(holder.counterSaleBO);
                                int inType = holder.pcsQty
                                        .getInputType();
                                holder.pcsQty
                                        .setInputType(InputType.TYPE_NULL);
                                holder.pcsQty.onTouchEvent(event);
                                holder.pcsQty.setInputType(inType);
                                holder.pcsQty.selectAll();
                                holder.pcsQty.requestFocus();

                                return true;
                            }
                        });


                holder.freePcs.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        QUANTITY = holder.freePcs;
                        QUANTITY.setTag(holder.counterSaleBO);
                        int inType = holder.freePcs
                                .getInputType();
                        holder.freePcs
                                .setInputType(InputType.TYPE_NULL);
                        holder.freePcs.onTouchEvent(motionEvent);
                        holder.freePcs.setInputType(inType);
                        holder.freePcs.selectAll();
                        holder.freePcs.requestFocus();

                        return true;
                    }
                });
                holder.freePcs
                        .addTextChangedListener(new TextWatcher() {

                            public void afterTextChanged(Editable s) {
                                String qty = s.toString();
                                if (!qty.equals("")) {
                                    int pc_qty = SDUtil
                                            .convertToInt(qty);
                                    if (pc_qty <= holder.counterSaleBO.getCsFreeSIH()) {
                                        holder.counterSaleBO.setCsFreePiece(pc_qty);
                                        holder.counterSaleBO.setCsFreeTotal((holder.counterSaleBO.getCsFreePiece() * holder.counterSaleBO.getMRP()));
                                        holder.txt_total.setText(SDUtil.format(holder.counterSaleBO.getCsFreeTotal(), 2, 0) + "");
                                        bmodel.productHelper.getProductMasterBOById(String.valueOf(holder.counterSaleBO.getParentid())).setCsFreeTotal(0);
                                    } else {
                                        qty = qty.length() > 1 ? qty.substring(0,
                                                qty.length() - 1) : "0";
                                        holder.counterSaleBO.setCsFreePiece(SDUtil
                                                .convertToInt(qty));

                                        holder.freePcs.setText(qty);
                                        Toast.makeText(mContext, mContext.getResources().getString(R.string.stock_not_available), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                            }
                        });

                holder.pcsQty
                        .addTextChangedListener(new TextWatcher() {

                            public void afterTextChanged(Editable s) {
                                String qty = s.toString();
                                if (!qty.equals("")) {
                                    int pc_qty = SDUtil
                                            .convertToInt(qty);

                                    if (pc_qty <= holder.counterSaleBO.getSIH()) {
                                        holder.counterSaleBO.setCsPiece(pc_qty);
                                        holder.counterSaleBO.setOrderedPcsQty(pc_qty);// for scheme

                                        holder.counterSaleBO.setCsTotal((holder.counterSaleBO.getCsPiece() * holder.counterSaleBO.getMRP()));
                                        holder.txt_total.setText(SDUtil.format(holder.counterSaleBO.getCsTotal(), 2, 0) + "");

                                        bmodel.productHelper.getProductMasterBOById(String.valueOf(holder.counterSaleBO.getParentid())).setCsTotal(0);
                                    } else {
                                        qty = qty.length() > 1 ? qty.substring(0,
                                                qty.length() - 1) : "0";
                                        holder.counterSaleBO.setCsPiece(SDUtil
                                                .convertToInt(qty));
                                        holder.counterSaleBO.setOrderedPcsQty(SDUtil
                                                .convertToInt(qty));// for scheme

                                        holder.pcsQty.setText(qty);
                                        Toast.makeText(mContext, mContext.getResources().getString(R.string.stock_not_available), Toast.LENGTH_SHORT).show();


                                    }
                                }
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                            }
                        });


                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                    }
                });

                row.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES)
                            if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG || bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
                                if (bmodel.schemeDetailsMasterHelper
                                        .getmSchemeList() == null
                                        || bmodel.schemeDetailsMasterHelper
                                        .getmSchemeList().size() == 0) {
                                    Toast.makeText(mContext,
                                            R.string.scheme_not_available,
                                            Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                bmodel.setActivity((Activity) mContext);

                                bmodel.productHelper.setSchemes(bmodel.schemeDetailsMasterHelper.getmSchemeList());
                                bmodel.productHelper.setPdname(counterBo.getProductName());
                                bmodel.productHelper.setProdId(counterBo.getProductID());
                                bmodel.productHelper.setProductObj(counterBo);
                                bmodel.productHelper.setFlag(1);
                                bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                                Intent intent = new Intent(mContext, CsProductSchemeDetailsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mContext.startActivity(intent);

                            }


                        return false;
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.counterSaleBO = counterBo;
            holder.pname = holder.counterSaleBO.getProductName();
            holder.psname.setText(holder.counterSaleBO.getProductShortName());
            holder.pcsQty.setText(holder.counterSaleBO.getCsPiece() + "");

            holder.txt_sih.setText(holder.counterSaleBO.getSIH() + "");
            holder.tv_barcode.setText(holder.counterSaleBO.getProductCode());
            holder.freePcs.setText(holder.counterSaleBO.getCsFreePiece() + "");
            holder.txt_free_sih.setText(holder.counterSaleBO.getCsFreeSIH() + "");
            holder.txt_mrp.setText(SDUtil.format(holder.counterSaleBO.getMRP(), 2, 0) + "");

            if (produuctType == PRODUCT_TYPE_FREE)
                holder.txt_total.setText("0");
            else
                holder.txt_total.setText(SDUtil.format((holder.counterSaleBO.getCsPiece() * holder.counterSaleBO.getMRP()), 2, 0) + "");

            return row;
        }

    }

    public class ViewHolder {
        private String productId, pname;
        private ProductMasterBO counterSaleBO;
        TextView psname, txt_sih, tv_barcode, txt_free_sih, txt_mrp, txt_total;
        private EditText pcsQty, freePcs;

    }

    private String getProductTotalValue(ProductMasterBO product) {
        double totalQty = 0;
        if (product.getCsPiece() > 0 || product.getCsCase() > 0 || product.getCsOuter() > 0) {
            totalQty = (product.getCsPiece() * product.getSrp()) + (product.getCsCase() * product.getCsrp()) + (product.getCsOuter() * product.getOsrp());
        }
        return bmodel.formatValue(totalQty);
    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        int val = 0;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getContext().getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt((String) QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
                val = s;
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt((String) append);
            }

        }
    }

    private View.OnClickListener mKeyPadListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            numPressed = true;

            if (QUANTITY == null) {
                bmodel.showAlert(mContext.getResources().getString(
                        R.string.please_select_item), 0);

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

    public interface ProductGroupInterface {
        public void onDismiss();
    }
}
