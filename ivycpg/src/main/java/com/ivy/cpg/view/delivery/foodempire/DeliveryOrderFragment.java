package com.ivy.cpg.view.delivery.foodempire;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CustomKeyBoard;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by rajkumar.s on 9/18/2017.
 */

public class DeliveryOrderFragment extends IvyBaseFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    View view;
    DrawerLayout mDrawerLayout;
    BusinessModel bmodel;
    Vector<ProductMasterBO> mylist;
    ListView listView;
    private EditText QUANTITY;
    private InputMethodManager inputManager;
    private String append = "";
    EditText mEdt_searchproductName;
    ViewFlipper viewFlipper;

    private Button mBtn_Search;
    private Button mBtnFilterPopup;
    private Button mBtn_clear;
    private Button btnNext;
    SearchAsync searchAsync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_delivery_order, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);

        listView = view.findViewById(
                R.id.listview);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        viewFlipper = view.findViewById(R.id.view_flipper);

        mEdt_searchproductName = view.findViewById(R.id.edt_searchproductName);
        mEdt_searchproductName.setOnEditorActionListener(this);

        mBtn_Search = view.findViewById(R.id.btn_search);
        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup = view.findViewById(R.id.btn_filter_popup);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear = view.findViewById(R.id.btn_clear);
        mBtn_clear.setOnClickListener(this);

        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        productName = view.findViewById(R.id.productName);
        productName.setTypeface(FontUtils.getProductNameFont(getActivity()));
        mEdt_searchproductName.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {

            setHasOptionsMenu(true);
            setScreenTitle(bmodel.mSelectedActivityName);

            mSearchTypeArray = new ArrayList<>();
            mSearchTypeArray.add(getResources().getString(R.string.all));
            mSearchTypeArray.add(getResources().getString(R.string.product_name));
            mSearchTypeArray.add(getResources().getString(R.string.prod_code));
            mSearchTypeArray.add(getResources().getString(
                    R.string.order_dialog_barcode));

            loadProducts();

            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 3) {
                        searchAsync = new SearchAsync();
                        searchAsync.execute();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    if (mEdt_searchproductName.getText().toString().length() < 3) {
                        mylist.clear();
                    }
                    if (searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
                        searchAsync.cancel(true);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }
            });

            searchAsync = new SearchAsync();

        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            bmodel.productHelper.clearOrderTable();
            startActivity(new Intent(getActivity(), HomeScreenTwo.class));
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    private void loadProducts() {
        try {
            mylist = new Vector<>();
            for (ProductMasterBO productMasterBO : bmodel.productHelper.getProductMaster()) {
                if (productMasterBO.getOrderedOuterQty() > 0 || productMasterBO.getOrderedCaseQty() > 0 || productMasterBO.getOrderedPcsQty() > 0) {
                    mylist.add(productMasterBO);
                }
            }

            MyAdapter mSchedule = new MyAdapter(mylist);
            listView.setAdapter(mSchedule);
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private class SearchAsync extends
            AsyncTask<Integer, Integer, Boolean> {


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            loadSearchedList();

            return true;
        }

        protected void onPostExecute(Boolean result) {

            MyAdapter mSchedule = new MyAdapter(mylist);
            listView.setAdapter(mSchedule);
        }
    }

    private void loadSearchedList() {
        try {

            mylist = new Vector<>();
            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            String mSelectedFilter = bmodel.getProductFilter();

            for (ProductMasterBO ret : bmodel.productHelper.getProductMaster()) {
                // For breaking search..
                if (searchAsync.isCancelled()) {
                    break;
                }

                if (ret.getOrderedOuterQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedPcsQty() > 0) {

                    if (mSelectedFilter.equals(getResources().getString(
                            R.string.order_dialog_barcode))) {

                        if (ret.getBarCode() != null
                                && (ret.getBarCode().toLowerCase()
                                .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                                || ret.getCasebarcode().toLowerCase().
                                contains(mEdt_searchproductName.getText().toString().toLowerCase())
                                || ret.getOuterbarcode().toLowerCase().
                                contains(mEdt_searchproductName.getText().toString().toLowerCase())) && ret.getIsSaleable() == 1) {
                            mylist.add(ret);


                        }
                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.prod_code))) {
                        if ((ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()) || (ret.getProductCode() != null
                                && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString().toLowerCase())))
                                && ret.getIsSaleable() == 1) {
                            mylist.add(ret);

                        }
                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.product_name))) {
                        if (ret.getProductShortName() != null && ret.getProductShortName()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()) && ret.getIsSaleable() == 1)
                            mylist.add(ret);
                    } else {
                        if (ret.getBarCode() != null
                                && (ret.getBarCode().toLowerCase()
                                .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                                || ret.getCasebarcode().toLowerCase().
                                contains(mEdt_searchproductName.getText().toString().toLowerCase())
                                || ret.getOuterbarcode().toLowerCase().
                                contains(mEdt_searchproductName.getText().toString().toLowerCase())) && ret.getIsSaleable() == 1) {
                            mylist.add(ret);


                        } else if ((ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()) || (ret.getProductCode() != null
                                && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString().toLowerCase())))
                                && ret.getIsSaleable() == 1) {
                            mylist.add(ret);

                        } else if (ret.getProductShortName() != null && ret.getProductShortName()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()) && ret.getIsSaleable() == 1)
                            mylist.add(ret);
                    }


                }
            }

        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final Vector<ProductMasterBO> items;
        private CustomKeyBoard dialogCustomKeyBoard;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(getActivity(),
                    R.layout.row_delivery_order, items);
            this.items = items;
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public @NonNull
        View getView(final int position, View convertView,
                     @NonNull ViewGroup parent) {
            final ViewHolder holder;
            ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_delivery_order, parent,
                        false);
                holder = new ViewHolder();


                holder.psname = row
                        .findViewById(R.id.stock_and_order_listview_productname);

                holder.caseQty = row
                        .findViewById(R.id.stock_and_order_listview_case_qty);
                holder.pcsQty = row
                        .findViewById(R.id.stock_and_order_listview_pcs_qty);
                holder.outerQty = row
                        .findViewById(R.id.stock_and_order_listview_outer_case_qty);

                holder.tv_pcs_ordered = row
                        .findViewById(R.id.tv_ordered_pcs);
                holder.tv_case_ordered = row
                        .findViewById(R.id.tv_ordered_case);
                holder.tv_outer_ordered = row
                        .findViewById(R.id.tv_ordered_outer);

                holder.sih = row
                        .findViewById(R.id.tv_sih);


                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                //setting typefaces
                holder.psname.setTypeface(FontUtils.getProductNameFont(getActivity()));

                holder.caseQty.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.pcsQty.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.outerQty.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));


                holder.tv_pcs_ordered.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.tv_case_ordered.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.tv_outer_ordered.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));


                ((TextView) row.findViewById(R.id.sihTitle)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.sih.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));


                // Order Field - Enable/Disable
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                    (row.findViewById(R.id.ll_ordered_case)).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                        ((TextView) row.findViewById(R.id.tv_ordered_case_Title)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));

                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_ordered_case_Title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_ordered_case_Title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_ordered_case_Title).getTag()));

                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    (row.findViewById(R.id.llPcs)).setVisibility(View.GONE);
                    (row.findViewById(R.id.ll_ordered_pcs)).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                        ((TextView) row.findViewById(R.id.tv_ordered_pcs_Title)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));

                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_ordered_pcs_Title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_ordered_pcs_Title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_ordered_pcs_Title).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                    (row.findViewById(R.id.ll_ordered_outer)).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) row.findViewById(R.id.outerTitle)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                        ((TextView) row.findViewById(R.id.tv_ordered_outer_Title)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outerTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outerTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outerTitle)
                                                    .getTag()));

                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_ordered_outer_Title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_ordered_outer_Title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_ordered_outer_Title)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        QUANTITY = holder.pcsQty;
                        QUANTITY.setTag(holder.productObj);
                        holder.pcsQty.selectAll();
                        holder.pcsQty.requestFocus();

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.showPrevious();

                        }
                    }
                });

                holder.caseQty.addTextChangedListener(new TextWatcher() {
                    @SuppressLint("StringFormatInvalid")
                    public void afterTextChanged(Editable s) {
                        if (holder.productObj.getCaseSize() == 0) {
                            holder.caseQty.removeTextChangedListener(this);
                            holder.caseQty.setText("0");
                            holder.caseQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.caseQty.setSelection(qty.length());

                        if (!"".equals(qty)) {

                            float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                                    .getCaseSize())
                                    + (holder.productObj.getDeliveredOuterQty() * holder.productObj
                                    .getOutersize())
                                    + holder.productObj.getDeliveredPcsQty();

                            float totalOrderedQty = (holder.productObj.getOrderedOuterQty() * holder.productObj
                                    .getOutersize())
                                    + (holder.productObj.getOrderedPcsQty())
                                    + (holder.productObj.getOrderedCaseQty() * holder.productObj.getCaseSize());

                            if (totalQty <= totalOrderedQty) {

                                if (totalQty <= holder.productObj.getSIH()) {

                                    holder.productObj.setDeliveredCaseQty(SDUtil
                                            .convertToInt(qty));

                                } else {

                                    if (!"0".equals(qty)) {

                                        Toast.makeText(
                                                getActivity(),
                                                getResources().getString(
                                                        R.string.stock_not_available),
                                                Toast.LENGTH_SHORT).show();

                                        //Delete the last entered number and reset the qty
                                        qty = qty.length() > 1 ? qty.substring(0,
                                                qty.length() - 1) : "0";

                                        holder.caseQty.setText(qty);

                                        holder.productObj.setDeliveredCaseQty(SDUtil
                                                .convertToInt(qty));
                                    }
                                }
                            } else {

                                if (!"0".equals(qty)) {

                                    Toast.makeText(
                                            getActivity(),
                                            getResources().getString(
                                                    R.string.exceeds_orderd_qty),
                                            Toast.LENGTH_SHORT).show();

                                    //Delete the last entered number and reset the qty
                                    qty = qty.length() > 1 ? qty.substring(0,
                                            qty.length() - 1) : "0";

                                    holder.caseQty.setText(qty);

                                    holder.productObj.setDeliveredOuterQty(SDUtil
                                            .convertToInt(qty));
                                }

                            }
                        } else {
                            holder.caseQty.setText("0");
                            holder.productObj.setDeliveredCaseQty(0);

                        }


                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                    holder.caseQty.setFocusable(false);

                    holder.caseQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                          /*  if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);*/

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), holder.caseQty);
                                dialogCustomKeyBoard.show();
                                dialogCustomKeyBoard.setCancelable(false);

                                //Grab the window of the dialog, and change the width
                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                Window window = dialogCustomKeyBoard.getWindow();
                                lp.copyFrom(window.getAttributes());
                                lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                window.setAttributes(lp);
                            }
                        }
                    });
                } else {
                    holder.caseQty.setFocusable(true);

                    holder.caseQty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                           /* if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);*/

                            QUANTITY = holder.caseQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.caseQty.getInputType();
                            holder.caseQty.setInputType(InputType.TYPE_NULL);
                            holder.caseQty.onTouchEvent(event);
                            holder.caseQty.setInputType(inType);
                            holder.caseQty.requestFocus();
                            if (holder.caseQty.getText().length() > 0)
                                holder.caseQty.setSelection(holder.caseQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }

                holder.pcsQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (holder.productObj.getPcUomid() == 0) {
                            holder.pcsQty.removeTextChangedListener(this);
                            holder.pcsQty.setText("0");
                            holder.pcsQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.pcsQty.setSelection(qty.length());
                        if (!"".equals(qty)) {

                            float totalQty = SDUtil.convertToInt(qty)
                                    + (holder.productObj.getDeliveredOuterQty() * holder.productObj
                                    .getOutersize())
                                    + (holder.productObj.getDeliveredCaseQty() * holder.productObj.getCaseSize());

                            float totalOrderedQty = (holder.productObj.getOrderedOuterQty() * holder.productObj
                                    .getOutersize())
                                    + (holder.productObj.getOrderedPcsQty())
                                    + (holder.productObj.getOrderedCaseQty() * holder.productObj.getCaseSize());

                            if (totalQty <= totalOrderedQty) {

                                if (totalQty <= holder.productObj.getSIH()) {

                                    holder.productObj.setDeliveredPcsQty(SDUtil
                                            .convertToInt(qty));

                                } else {

                                    if (!"0".equals(qty)) {

                                        Toast.makeText(
                                                getActivity(),
                                                getResources().getString(
                                                        R.string.stock_not_available),
                                                Toast.LENGTH_SHORT).show();

                                        //Delete the last entered number and reset the qty
                                        qty = qty.length() > 1 ? qty.substring(0,
                                                qty.length() - 1) : "0";

                                        holder.pcsQty.setText(qty);

                                        holder.productObj.setDeliveredPcsQty(SDUtil
                                                .convertToInt(qty));
                                    }
                                }
                            } else {

                                if (!"0".equals(qty)) {

                                    Toast.makeText(
                                            getActivity(),
                                            getResources().getString(
                                                    R.string.exceeds_orderd_qty),
                                            Toast.LENGTH_SHORT).show();

                                    //Delete the last entered number and reset the qty
                                    qty = qty.length() > 1 ? qty.substring(0,
                                            qty.length() - 1) : "0";

                                    holder.pcsQty.setText(qty);

                                    holder.productObj.setDeliveredOuterQty(SDUtil
                                            .convertToInt(qty));
                                }

                            }

                        } else {
                            holder.pcsQty.setText("0");
                            holder.productObj.setDeliveredPcsQty(0);

                        }


                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });
                if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                    holder.pcsQty.setFocusable(false);

                    holder.pcsQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
/*
                            if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :" + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);*/

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), holder.pcsQty);
                                dialogCustomKeyBoard.show();
                                dialogCustomKeyBoard.setCancelable(false);

                                //Grab the window of the dialog, and change the width
                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                Window window = dialogCustomKeyBoard.getWindow();
                                lp.copyFrom(window.getAttributes());
                                lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                window.setAttributes(lp);
                            }
                        }
                    });
                } else {
                    holder.pcsQty.setFocusable(true);

                    holder.pcsQty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                           /* if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);*/

                            QUANTITY = holder.pcsQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.pcsQty.getInputType();
                            holder.pcsQty.setInputType(InputType.TYPE_NULL);
                            holder.pcsQty.onTouchEvent(event);
                            holder.pcsQty.setInputType(inType);
                            holder.pcsQty.requestFocus();
                            if (holder.pcsQty.getText().length() > 0)
                                holder.pcsQty.setSelection(holder.pcsQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }

                holder.outerQty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.productObj.getOuUomid() == 0) {
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText("0");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }


                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.outerQty.setSelection(qty.length());
                        if (!"".equals(qty)) {

                            float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                                    .getOutersize())
                                    + (holder.productObj.getDeliveredPcsQty())
                                    + (holder.productObj.getDeliveredCaseQty() * holder.productObj.getCaseSize());

                            float totalOrderedQty = (holder.productObj.getOrderedOuterQty() * holder.productObj
                                    .getOutersize())
                                    + (holder.productObj.getOrderedPcsQty())
                                    + (holder.productObj.getOrderedCaseQty() * holder.productObj.getCaseSize());

                            if (totalQty <= totalOrderedQty) {

                                if (totalQty <= holder.productObj.getSIH()) {

                                    holder.productObj.setDeliveredOuterQty(SDUtil
                                            .convertToInt(qty));

                                } else {

                                    if (!"0".equals(qty)) {

                                        Toast.makeText(
                                                getActivity(),
                                                getResources().getString(
                                                        R.string.stock_not_available),
                                                Toast.LENGTH_SHORT).show();

                                        //Delete the last entered number and reset the qty
                                        qty = qty.length() > 1 ? qty.substring(0,
                                                qty.length() - 1) : "0";

                                        holder.outerQty.setText(qty);

                                        holder.productObj.setDeliveredOuterQty(SDUtil
                                                .convertToInt(qty));
                                    }
                                }
                            } else {

                                if (!"0".equals(qty)) {

                                    Toast.makeText(
                                            getActivity(),
                                            getResources().getString(
                                                    R.string.exceeds_orderd_qty),
                                            Toast.LENGTH_SHORT).show();

                                    //Delete the last entered number and reset the qty
                                    qty = qty.length() > 1 ? qty.substring(0,
                                            qty.length() - 1) : "0";

                                    holder.outerQty.setText(qty);

                                    holder.productObj.setDeliveredOuterQty(SDUtil
                                            .convertToInt(qty));
                                }

                            }
                        } else {
                            holder.outerQty.setText("0");
                            holder.productObj.setDeliveredOuterQty(0);

                        }


                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                });
                if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                    holder.outerQty.setFocusable(false);

                    holder.outerQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

/*
                            if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);
*/

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), holder.outerQty);
                                dialogCustomKeyBoard.show();
                                dialogCustomKeyBoard.setCancelable(false);

                                //Grab the window of the dialog, and change the width
                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                Window window = dialogCustomKeyBoard.getWindow();
                                lp.copyFrom(window.getAttributes());
                                lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                window.setAttributes(lp);
                            }
                        }
                    });
                } else {
                    holder.outerQty.setFocusable(true);

                    holder.outerQty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                         /*   if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);*/

                            QUANTITY = holder.outerQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.outerQty.getInputType();
                            holder.outerQty.setInputType(InputType.TYPE_NULL);
                            holder.outerQty.onTouchEvent(event);
                            holder.outerQty.setInputType(inType);
                            holder.outerQty.requestFocus();
                            if (holder.outerQty.getText().length() > 0)
                                holder.outerQty.setSelection(holder.outerQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productObj = product;

            holder.psname.setText(holder.productObj.getProductShortName());

            // Set order qty
            if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                String strCaseQty = holder.productObj.getDeliveredCaseQty() + "";
                holder.caseQty.setText(strCaseQty);
            }
            if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                String strPcsQty = holder.productObj.getDeliveredPcsQty() + "";
                holder.pcsQty.setText(strPcsQty);
            }
            if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                String strOuterQty = holder.productObj.getDeliveredOuterQty() + "";
                holder.outerQty.setText(strOuterQty);
            }


            holder.sih.setText(String.valueOf(holder.productObj.getSIH()));


            holder.tv_outer_ordered.setText(String.valueOf(holder.productObj.getOrderedOuterQty()));
            holder.tv_case_ordered.setText(String.valueOf(holder.productObj.getOrderedCaseQty()));
            holder.tv_pcs_ordered.setText(String.valueOf(holder.productObj.getOrderedPcsQty()));


            return row;
        }
    }

    class ViewHolder {
        private ProductMasterBO productObj;
        private TextView psname, sih;

        private TextView tv_pcs_ordered, tv_case_ordered, tv_outer_ordered;

        private EditText pcsQty;
        private EditText caseQty;
        private EditText outerQty;

    }


    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        //  int val;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                String strS = s + "";
                QUANTITY.setText(strS);
                // val = s;


            } else {
                Button ed = getActivity().findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                // val = SDUtil.convertToInt(append);
            }


        }
    }

    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private TextView productName;

    @Override
    public void onClick(View view) {

        Button vw = (Button) view;

        if (vw == mBtn_Search) {
            viewFlipper.showNext();

        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });

            int selectedFiltPos = mSearchTypeArray.indexOf(bmodel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }

                    });
            builderSingle.setPositiveButton(
                    getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                        }
                    });


            bmodel.applyAlertDialogTheme(builderSingle);

        } else if (vw == mBtn_clear) {
            viewFlipper.showPrevious();
            mEdt_searchproductName.setText("");
            productName.setText("");
            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
            loadProducts();

        } else if (vw == btnNext) {
            if (isDelivering()) {
                if (isPartialDelivery()) {
                    //to summary
                    Intent intent = new Intent(getActivity(), DeliveryOrderSummary.class);
                    intent.putExtra("isPartial", true);
                    startActivity(intent);
                    getActivity().finish();

                } else {
                    //to
                    SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext());
                    if (schemeHelper.getAppliedSchemeList() != null && schemeHelper.getAppliedSchemeList().size() > 0) {
                        //scheme available

                        boolean isStockAvailableForScheme = true;
                        boolean isFreeProductsAvailable = false;
                        for (SchemeBO schemeBO : schemeHelper.getAppliedSchemeList()) {

                            if (schemeBO.isQuantityTypeSelected()) {
                                isFreeProductsAvailable = true;
                                for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts()) {

                                    ProductMasterBO productMasterBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                                    int totalQtyNeeded = (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize())
                                            + (productMasterBO.getOrderedPcsQty())
                                            + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize());

                                    if (schemeProductBO.getUomID() == productMasterBO.getPcUomid()) {
                                        totalQtyNeeded += schemeProductBO.getQuantitySelected();
                                    } else if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                                        totalQtyNeeded += (schemeProductBO.getQuantitySelected() * productMasterBO.getCaseSize());
                                    } else if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                                        totalQtyNeeded += (schemeProductBO.getQuantitySelected() * productMasterBO.getOutersize());
                                    }

                                    if (totalQtyNeeded > productMasterBO.getSIH()) {
                                        isStockAvailableForScheme = false;
                                        break;
                                    }
                                }
                            }

                        }
                        if (isFreeProductsAvailable) {
                            if (isStockAvailableForScheme) {
                                Intent intent = new Intent(getActivity(), DeliveryOrderScheme.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                //No stock available for scheme free products..

                                new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                                        getResources().getString(R.string.stock_not_availble_for_free_product),
                                        getResources().getString(R.string.do_want_to_continue_with_partial_invoice),
                                        false, getActivity().getResources().getString(R.string.ok),
                                        getActivity().getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                                    @Override
                                    public void onPositiveButtonClick() {

                                        Intent intent = new Intent(getActivity(), DeliveryOrderSummary.class);
                                        intent.putExtra("isPartial", true);
                                        startActivity(intent);
                                        getActivity().finish();

                                    }
                                }, new CommonDialog.negativeOnClickListener() {
                                    @Override
                                    public void onNegativeButtonClick() {
                                    }
                                }).show();


                            }

                        } else {
                            // No free products
                            Intent intent = new Intent(getActivity(), DeliveryOrderSummary.class);
                            intent.putExtra("isPartial", false);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    } else {
                        //No scheme
                        Intent intent = new Intent(getActivity(), DeliveryOrderSummary.class);
                        intent.putExtra("isPartial", false);
                        startActivity(intent);
                        getActivity().finish();
                    }

                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_data_exists), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (mEdt_searchproductName.getText().length() >= 3) {
                searchAsync = new SearchAsync();
                searchAsync.execute();
            } else {
                Toast.makeText(getActivity(), "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        return false;
    }

    private boolean isPartialDelivery() {
        try {
            for (ProductMasterBO bo : mylist) {
                if (bo.getOrderedOuterQty() > 0 || bo.getOrderedCaseQty() > 0 || bo.getOrderedPcsQty() > 0) {

                    float totalDeliverQty = (bo.getDeliveredOuterQty() * bo.getOutersize())
                            + (bo.getDeliveredPcsQty())
                            + (bo.getDeliveredCaseQty() * bo.getCaseSize());

                    float totalOrderedQty = (bo.getOrderedOuterQty() * bo.getOutersize())
                            + (bo.getOrderedPcsQty())
                            + (bo.getOrderedCaseQty() * bo.getCaseSize());

                    if (totalOrderedQty != totalDeliverQty) {
                        return true;
                    }

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return false;
    }

    private boolean isDelivering() {
        try {
            for (ProductMasterBO bo : mylist) {
                if (bo.getOrderedOuterQty() > 0 || bo.getOrderedCaseQty() > 0 || bo.getOrderedPcsQty() > 0) {
                    if (bo.getDeliveredOuterQty() > 0 || bo.getDeliveredCaseQty() > 0 || bo.getDeliveredPcsQty() > 0) {
                        return true;
                    }

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return false;
    }

}
