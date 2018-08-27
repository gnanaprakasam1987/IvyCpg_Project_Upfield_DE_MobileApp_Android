package com.ivy.cpg.view.salesreturn;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.CustomKeyBoard;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class SalesReturnFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener, FiveLevelFilterCallBack {

    private static final int SALES_RET_SUMMARY = 1;
    private static final int SALES_ENTRY = 2;
    private double totalvalue = 0;
    private ProductMasterBO productMasterBO;
    private SalesReturnHelper salesReturnHelper;
    private String BRAND_STRING = "Brand";
    private TextView pnametitle;
    private View view;
    private DrawerLayout mDrawerLayout;
    private BusinessModel bmodel;
    FrameLayout drawer;
    private ViewFlipper viewFlipper;
    private ArrayList<ProductMasterBO> mylist;
    private ArrayList<String> fiveFilter_productIDs;
    private Vector<ProductMasterBO> items;
    private String brandbutton;
    private int mSelectedBrandID = 0;
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private String generalbutton = GENERAL;
    private Button mBtn_Search, mBtn_clear;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    public ListView lvwplist;
    public String strBarCodeSearch = "ALL";
    public EditText mEdt_searchproductName;
    public TextView totalValueText, lpcText, productName;
    Button btn_next;
    public String mSelectedFilter;
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    Button mBtnFilterPopup;
    private String screenTitle = "";
    private boolean loadBothSalable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_salesreturn_header, container,
                false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);

        //setting drawer width equal to scren width
        drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        screenTitle = getActivity().getIntent().getStringExtra("screentitle");


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        setHasOptionsMenu(true);
        PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(getContext());
        priceTrackingHelper.mSelectedFilter = -1;
    }


    @Override
    public void onStart() {

        super.onStart();

        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());

        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        productName = (TextView) view.findViewById(R.id.productName);
        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        mEdt_searchproductName = (EditText) view.findViewById(R.id.edt_searchproductName);
        mBtn_Search = (Button) view.findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) view.findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) view.findViewById(R.id.btn_clear);
        btn_next = (Button) view.findViewById(R.id.btn_save);


        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        mBtn_clear.setOnEditorActionListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);
        lvwplist = (ListView) view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        totalValueText = (TextView) view.findViewById(R.id.totalValue);
        lpcText = (TextView) view.findViewById(R.id.lcp);

        lpcText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalValueText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        btn_next.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.totalTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.totalTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.totalTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        setScreenTitle(screenTitle);

        pnametitle = (TextView) view.findViewById(R.id.tvProductNameTitle);
        ((TextView) view.findViewById(R.id.tvProductNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.totalTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        loadBothSalable = salesReturnHelper.SHOW_SALABLE_AND_NON_SALABLE_SKU;

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout,
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(screenTitle);
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);


        updateBrandText(BRAND_STRING, -1);


        mDrawerLayout.closeDrawer(GravityCompat.END);

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 3) {
                        loadSearchedList();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }


        mBtnFilterPopup = (Button) view.findViewById(R.id.btn_filter_popup);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }


    public void refreshList() {
        String strPname = getResources().getString(
                R.string.product_name)
                + " (" + mylist.size() + ")";
        pnametitle.setText(strPname);
        // OutletListAdapter lvwplist = new OutletListAdapter(mylist);
        lvwplist.setAdapter(new MyAdapter(mylist));
        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtn_clear) {
            if (mEdt_searchproductName.getText().length() > 0) {
                mEdt_searchproductName.setText("");
                strBarCodeSearch = "ALL";
            }
            loadProductList();

            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
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

        } else if (vw == btn_next) {
            onNextButtonClick();
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                getActivity().supportInvalidateOptionsMenu();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEdt_searchproductName.getWindowToken(), 0);
            }
            loadSearchedList();
            return true;
        }
        return false;
    }

    public void loadSearchedList() {
        if (generalbutton == null)
            generalbutton = GENERAL;
        ProductMasterBO ret;
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = salesReturnHelper.getSalesReturnProducts();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<>();
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ret = items.elementAt(i);
                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);

                        Commons.print("siz Barcode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    }
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_gcas))) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);

                        Commons.print("siz GCASCode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    }
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.product_name))) {
                    Commons.print("siz product_name : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);

                    }
                }
            }
            if (mylist.size() == 0)
                Toast.makeText(getActivity(), getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
            refreshList();
        } else {
            Toast.makeText(getActivity(), "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void loadProductList() {
        try {
            Vector<ProductMasterBO> items = salesReturnHelper.getSalesReturnProducts();

            int siz = items.size();
            mylist = new ArrayList<>();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (loadBothSalable ?
                        (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0) : ret.getIsSaleable() == 1) {
                    if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);

                }
            }
            refreshList();
            updateValue();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {

        private final ArrayList<ProductMasterBO> items;
        private CustomKeyBoard dialogCustomKeyBoard;

        MyAdapter(ArrayList<ProductMasterBO> items) {

            super(getActivity(), R.layout.row_salesreturn,
                    items);
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


        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            productMasterBO = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());

                row = inflater.inflate(R.layout.row_salesreturn, parent, false);

                holder = new ViewHolder();

                holder.psname = (TextView) row.findViewById(R.id.productName);
                holder.productCode = (TextView) row.findViewById(R.id.sales_return_barcode);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.total = (TextView) row.findViewById(R.id.total);

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.productCode.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.total.setPaintFlags(holder.total.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.productCode.setVisibility(View.GONE);

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                        productName.setText(holder.pname);


                        if (viewFlipper.getDisplayedChild() != 0) {
                           /* viewFlipper.setInAnimation(
                                    SalesReturnFragment.this,
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(
                                    SalesReturnFragment.this,
                                    R.anim.out_to_left);*/
                            viewFlipper.showPrevious();
                        }
                    }
                });

                holder.total.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        View vChild = lvwplist.getChildAt(0);
                        int holderPosition = lvwplist.getFirstVisiblePosition();
                        int holderTop = (vChild == null) ? 0 : (vChild.getTop() - lvwplist.getPaddingTop());

                        productName.setText(holder.pname);
                        showSalesReturnDialog(holder.productBO.getProductID(), v, holderPosition, holderTop);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = productMasterBO;
            if (holder.productBO.getSalesReturnReasonList() != null && holder.productBO.getSalesReturnReasonList().size() != 0)
                holder.reasonBO = holder.productBO.getSalesReturnReasonList().get(holder.productBO.getSelectedSalesReturnPosition());

            holder.pname = productMasterBO.getProductName();
            holder.psname.setText(productMasterBO.getProductShortName());
            if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code) + ": " +
                        productMasterBO.getProductCode();
                holder.productCode.setText(prodCode);
            }


            int total = 0;
            for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
                total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
            String strTotal = Integer.toString(total);
            holder.total.setText(strTotal);
            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_odd_item_bg));
            }
            return row;
        }
    }

    class ViewHolder {
        private SalesReturnReasonBO reasonBO;
        private ProductMasterBO productBO;
        private String pname;
        private TextView psname, productCode;
        private TextView total;
    }

    private void updateValue() {
        totalvalue = 0;
        int lpccount = 0;
        Vector<ProductMasterBO> items = salesReturnHelper.getSalesReturnProducts();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        if (siz == 0)
            return;

        boolean lineflag;
        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = items.elementAt(i);

            lineflag = false;
            for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {
                double temp;
                //if (bo.getSrpedit() > 0) {
                if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                        || bo.getOuterQty() > 0) {
                    lineflag = true;
                    temp = ((bo.getCaseQty() * bo.getCaseSize())
                            + (bo.getOuterQty() * bo.getOuterSize()) + bo
                            .getPieceQty()) * bo.getSrpedit();
                    totalvalue = totalvalue + temp;
                }
                /*} else {
                    if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                            || bo.getOuterQty() > 0) {
                        lineflag = true;
                        temp = (bo.getCaseQty() * ret.getCsrp())
                                + (bo.getPieceQty() * ret.getSrp())
                                + (bo.getOuterQty() * ret.getOsrp());
                        totalvalue = totalvalue + temp;
                    }
                }*/
            }
            if (lineflag) {
                lpccount = lpccount + 1;
            }
        }
        String strLpcCount = Integer.toString(lpccount);
        lpcText.setText(strLpcCount);
        String strTotalValue = bmodel.formatValue(totalvalue) + "";
        totalValueText.setText(strTotalValue);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
//                CommonDialog dialog=
                new CommonDialog(getActivity().getApplicationContext(), getActivity(), "", getResources().getString(
                        R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        salesReturnHelper
                                .clearSalesReturnTable(false);
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(SDUtil
                                        .now(SDUtil.TIME));
                        getActivity().finish();
                        //BusinessModel.loadActivity(
                               // getActivity(),
                              //  DataMembers.actHomeScreenTwo);
                      Intent  myIntent = new Intent(getActivity(), HomeScreenTwo.class);
                       startActivityForResult(myIntent, 0);

                        /* User clicked OK so do some stuff */
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
                break;
            default:
                break;
        }
        return null;
    }

    public void onNextButtonClick() {
        if (salesReturnHelper.hasSalesReturn()) {
            if (!isValidData()) {
                Toast.makeText(getActivity(), "Replace quantity should not exceed Return quantity", Toast.LENGTH_LONG).show();
                return;
            }

            if (salesReturnHelper.CHECK_MRP_VALUE) {
                if (!isValidMRP()) {
                    Toast.makeText(getActivity(), "Please enter MRP Value", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            double totalOrderValue = bmodel.getOrderValue();
            if (bmodel.configurationMasterHelper.IS_ORD_SR_VALUE_VALIDATE &&
                    !bmodel.configurationMasterHelper.IS_INVOICE &&
                    totalvalue >= totalOrderValue) {
                Toast.makeText(getActivity(),
                        getResources().getString(
                                R.string.sales_return_value_should_not_exceed_order_value,
                                String.valueOf(totalOrderValue)),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (bmodel.configurationMasterHelper.IS_SR_VALIDATE_BY_RETAILER_TYPE) {
                if (bmodel.retailerMasterBO.getRpTypeCode() != null && bmodel.retailerMasterBO.getRpTypeCode().equals("CASH")) {
                    if (OrderHelper.getInstance(getActivity()).returnReplacementAmountValidation(true,false,getActivity().getApplicationContext())) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.return_products_not_matching_replacing_product_price), Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else if (bmodel.retailerMasterBO.getRpTypeCode() != null && bmodel.retailerMasterBO.getRpTypeCode().equals("CREDIT")) {
                    if (!OrderHelper.getInstance(getActivity()).returnReplacementAmountValidation(false,false,getActivity().getApplicationContext())) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.return_products_price_less_than_replacing_product_price), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            salesReturnHelper.setLpcValue((String) lpcText.getText());
            salesReturnHelper.setReturnValue(totalvalue);

            Intent init = new Intent(getActivity(),
                    SalesReturnSummery.class);
            startActivityForResult(init, SALES_RET_SUMMARY);

        } else {
            bmodel.showAlert(
                    getResources().getString(R.string.no_items_added), 0);
        }
    }

    private boolean isValidData() {
        Vector<ProductMasterBO> items = salesReturnHelper.getSalesReturnProducts();
        int totalRetQty = 0;
        int totalRepQty;
        int siz = items.size();

        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getRepPieceQty() > 0 || ret.getRepCaseQty() > 0 || ret.getRepOuterQty() > 0) {

                List<SalesReturnReasonBO> reasonList = ret.getSalesReturnReasonList();
                for (SalesReturnReasonBO salesReturnReasonBO : reasonList) {
                    totalRetQty = totalRetQty + (salesReturnReasonBO.getCaseQty() * ret.getCaseSize())
                            + (salesReturnReasonBO.getPieceQty())
                            + (salesReturnReasonBO.getOuterQty() * ret.getOutersize());
                }

                totalRepQty = (ret.getRepCaseQty() * ret.getCaseSize())
                        + (ret.getRepPieceQty())
                        + (ret.getRepOuterQty() * ret.getOutersize());
                if (totalRetQty < totalRepQty)
                    return false;
            }
        }
        return true;
    }

    private boolean isValidMRP() {
        int siz = salesReturnHelper.getSalesReturnProducts().size();
        if (siz == 0)
            return true;

        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = salesReturnHelper.getSalesReturnProducts().get(i);
            if (product.getSalesReturnReasonList() == null || product.getSalesReturnReasonList().size() == 0)
                return true;
            for (SalesReturnReasonBO bo : product
                    .getSalesReturnReasonList()) {
                if (bo.getCaseQty() > 0 || bo.getPieceQty() > 0 || bo.getOuterQty() > 0) {
                    if (bo.getOldMrp() == 0)
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);


        menu.findItem(R.id.menu_fivefilter).setVisible(true);
        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_barcode).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_scheme).setVisible(false);
        menu.findItem(R.id.menu_apply_so).setVisible(false);
        menu.findItem(R.id.menu_apply_std_qty).setVisible(false);
        menu.findItem(R.id.menu_sih_apply).setVisible(false);
        menu.findItem(R.id.menu_next).setIcon(
                R.drawable.ic_action_navigation_next_item);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);

        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);

        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        if (drawerOpen)
            menu.clear();


        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //clear sign details once sales return is exited to start fresh on next visit
                if (salesReturnHelper.getSignatureName() != null && !salesReturnHelper.getSignatureName().isEmpty()) {
                    String PHOTO_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;
                    bmodel.synchronizationHelper.deleteFiles(
                            PHOTO_PATH, salesReturnHelper.getSignatureName());
                }
                salesReturnHelper.setIsSignCaptured(false);
                salesReturnHelper.setSignatureName("");
                salesReturnHelper.setSignaturePath("");
                salesReturnHelper.setInvoiceId("");
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                    mDrawerLayout.closeDrawers();
                else
                    showCustomDialog();
                return true;

            case R.id.menu_fivefilter:
                FiveFilterFragment();
                return true;

            case R.id.menu_barcode:
                ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
                int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                        @Override
                        protected void startActivityForResult(Intent intent, int code) {
                            SalesReturnFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                        }
                    };
                    integrator.setBeepEnabled(false).initiateScan();
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.permission_enable_msg)
                                    + " " + getResources().getString(R.string.permission_camera)
                            , Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            if (salesReturnHelper.getFilterProductLevels() != null && salesReturnHelper.getFilterProductsByLevelId() != null)
                bundle.putString("isFrom", "SR");
            else
                bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = (mFilterText == null ? BRAND : mFilterText);

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            // Clear the productName
            productName.setText("");

            //items = getProducts();
            items = salesReturnHelper.getSalesReturnProducts();
            Commons.print("AS<><><><" + salesReturnHelper.getSalesReturnProducts().size());
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<>();
            mylist.clear();
            // Add the products into list
            int orderCount = -1;
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (bid == -1) {
                    if (BRAND_STRING.equals(mFilterText) || (bid == ret.getParentid())) {
                        int count = 0;
                        for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {

                            if (bo.getPieceQty() > 0 || bo.getCaseQty() > 0 || bo.getOuterQty() > 0) {
                                count = count + 1;
                                orderCount = orderCount + 1;
                                mylist.add(orderCount, ret);
                                break;
                            }
                        }
                        if (count > 0)
                            continue;
                    }
                } else if (bid == ret.getParentid()) {
                    int count = 0;
                    for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {
                        if (bo.getPieceQty() > 0 || bo.getCaseQty() > 0 || bo.getOuterQty() > 0) {
                            count = count + 1;
                            orderCount = orderCount + 1;
                            mylist.add(orderCount, ret);
                            break;
                        }
                    }
                    if (count > 0)
                        continue;
                }

                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || ("ALL").equals(strBarCodeSearch)) {

                    if (bid == -1 &&
                            (loadBothSalable
                                    ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                                    : ret.getIsSaleable() == 1)) {
                        if (mFilterText.equals(BRAND_STRING)) {
                            mylist.add(ret);
                        }
                    } else if (bid == ret.getParentid() &&
                            (loadBothSalable
                                    ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                                    : ret.getIsSaleable() == 1)) {
                        mylist.add(ret);
                    }

                }
            }
            refreshList();
            updateValue();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        fiveFilter_productIDs = null;
        generalbutton = mFilterText;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {

    }

    private void showCustomDialog() {

        new CommonDialog(getActivity().getApplicationContext(), getActivity(), "", getResources().getString(
                R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                salesReturnHelper.clearSalesReturnTable(false);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                startActivity(intent);
                getActivity().finish();
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
            }
        }).show();
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mylist = new ArrayList<>();
        fiveFilter_productIDs = new ArrayList<>();
        brandbutton = mFilterText;
        if (mAttributeProducts != null) {
            if (mFilteredPid != 0) {
                for (ProductMasterBO productBO : items) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/") && productBO.getIsSaleable() == 1) {
                        // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                        if (mAttributeProducts.contains(SDUtil.convertToInt(productBO.getProductID()))) {
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {
                        if (pid == SDUtil.convertToInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            }
        } else {
            if (!mFilterText.isEmpty()) {
                for (ProductMasterBO productBO : items) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/") && productBO.getIsSaleable() == 1) {
                        mylist.add(productBO);
                        fiveFilter_productIDs.add(productBO.getProductID());
                    }

                }
            } else {
                updateBrandText(BRAND, -1);
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();
        refreshList();
        updateValue();
    }

    private void showSalesReturnDialog(String productId, View v, int holderPostion, int holderTop) {
        Intent intent = new Intent(getActivity(),
                SalesReturnEntryActivity.class);
        intent.putExtra("pid", productId);
        intent.putExtra("position", holderPostion);
        intent.putExtra("top", holderTop);
        intent.putExtra("from", "SALESRETURN");

        ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.zoom_enter, R.anim.hold);
        ActivityCompat.startActivityForResult(getActivity(), intent, SALES_ENTRY, opts.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == SALES_RET_SUMMARY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                /*finish();
                BusinessModel.loadActivity(this,
                        DataMembers.actHomeScreenTwo);*/

                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                Bundle extras = getActivity().getIntent().getExtras();
                if (extras != null) {
                    intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                    intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));

                }

                startActivity(intent);
                getActivity().finish();
            }
        } else if (requestCode == SALES_ENTRY) {
            if (resultCode == RESULT_OK) {
                getActivity().overridePendingTransition(0, R.anim.zoom_exit);
                updateValue();
                refreshList();
                Bundle extras = data.getExtras();
                int holderPosition = extras.getInt("position", 0);
                int holderTop = extras.getInt("top", 0);
                if (mylist.size() > 0)
                    lvwplist.setSelectionFromTop(holderPosition, holderTop);
            }
        } else {
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    strBarCodeSearch = result.getContents();
                    if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                        bmodel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                        mEdt_searchproductName.setText(strBarCodeSearch);
                        if (viewFlipper.getDisplayedChild() == 0) {
                            viewFlipper.showNext();
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

}