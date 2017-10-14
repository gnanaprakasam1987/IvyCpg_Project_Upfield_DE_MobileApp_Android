package com.ivy.countersales;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar.s on 18-03-2016.
 */
public class CSFreeSample extends IvyBaseActivityNoActionBar implements BrandDialogInterface, View.OnClickListener, TextView.OnEditorActionListener {

    BusinessModel bmodel;
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    private static final String GENERAL = "General";
    private static final String BRAND = "Brand";
    boolean isProductFilter_enabled = true;
    ListView listView;
    private TextView productName;
    private EditText QUANTITY, mEdt_searchproductName;
    private InputMethodManager inputManager;
    private Button mBtn_Search, mBtnFilterPopup, mBtn_clear;
    private ViewFlipper viewFlipper;
    public Vector<ProductMasterBO> items;
    public ArrayList<ProductMasterBO> mylist;
    public String brandbutton, generalbutton;
    private String mSelectedFilter;
    public ProductMasterBO ret;
    private String strBarCodeSearch = "ALL";
    private ArrayList<String> mSearchTypeArray = new ArrayList<String>();
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private String append = "";
    TextView txt_total_lines;
    private Toolbar toolbar;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cs_free_sample);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.mSelectedActivityName);

        mDrawerLayout = (DrawerLayout) findViewById(
                R.id.drawer_layout);
        listView = (ListView) findViewById(R.id.lvwplist);
        txt_total_lines = (TextView) findViewById(R.id.txt_total_lines);
        txt_total_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_total_lines)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar()
                        .setTitle(bmodel.mSelectedActivityName);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {

                getSupportActionBar()
                        .setTitle(getResources().getString(R.string.filter));
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        updategeneraltext(GENERAL);
        mDrawerLayout.closeDrawer(GravityCompat.END);
        bmodel.mSelectedActivityName = "Free Sample";

        inputManager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        mEdt_searchproductName = (EditText) findViewById(
                R.id.edt_searchproductName);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);
        productName = (TextView) findViewById(R.id.productName);
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        mSearchTypeArray = new ArrayList<String>();
        mSearchTypeArray.add("ProductName");
        //  mSearchTypeArray.add("GCAS Code");
        mSearchTypeArray.add("BarCode");

        // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            findViewById(R.id.caseTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.caseTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.caseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.caseTitle).getTag()));
            } catch (Exception e) {
                Commons.print("" + e);
            }
        }
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
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
            findViewById(R.id.outercaseTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outercaseTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.outercaseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.outercaseTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.print("" + e);
            }
        }

        searchText();

        saveBtn = (Button) findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(this);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
    }


    @Override
    public void onClick(View view) {
        Button vw = (Button) view;
        /*bmodel = (BusinessModel)getApplicationContext();
        bmodel.setContext(this);*/
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // mSelectedFilter = arrayAdapter.getItem(which);
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = mSearchTypeArray.indexOf(bmodel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // mSelectedFilter = arrayAdapter.getItem(which);
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
            mEdt_searchproductName.setText("");
            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");

            supportInvalidateOptionsMenu();
            updategeneraltext(GENERAL);
        } else if (vw == saveBtn) {
            getEnteredSampleProducts();
            finish();

        }
    }

   /* private void loadProducts(){
        lstProducts=new Vector<>();
        for(ProductMasterBO productMasterBO:bmodel.productHelper.getProductMaster()){
            lstProducts.add(productMasterBO);
        }

        MyAdapter adapter=new MyAdapter(lstProducts);
        listView.setAdapter(adapter);

    }*/

    private void searchText() {
        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    supportInvalidateOptionsMenu();
                    if (s.length() >= 3) {
                        loadSearchedList();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    // TODO Auto-generated method stub

                }
            });
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                brandbutton = BRAND;
                generalbutton = GENERAL;
                supportInvalidateOptionsMenu();
            }
            loadSearchedList();
            return true;
        }
        return false;
    }

    public void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<ProductMasterBO>();
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ret = (ProductMasterBO) items.elementAt(i);
                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                    Commons.print("siz Barcode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_gcas))) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                    Commons.print("siz GCASCode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.product_name))) {
                    Commons.print("siz product_name : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                }
            }
            refreshList();
        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void refreshList() {

        MyAdapter mSchedule = new MyAdapter(mylist);
        listView.setAdapter(mSchedule);

        updateTotalLines();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_next).setVisible(false);

        if (isProductFilter_enabled)
            menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_product_filter).setVisible(false);

        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        else
            menu.findItem(R.id.menu_product_filter).setVisible(true);

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_counter_sales, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                /*bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                startActivity(new Intent(this, CSHomeScreen.class));*/
                //getEnteredSampleProducts();
                finish();
            }
            return true;
        } else if (i == R.id.menu_next) {

            return true;
        } else if (i == R.id.menu_product_filter) {// brandFilterClicked();
            productFilterClickedFragment();
            supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void productFilterClickedFragment() {
        try {

            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            if (bmodel.productHelper.getChildLevelBo().size() > 0)
                bundle.putString("filterHeader", bmodel.productHelper
                        .getChildLevelBo().get(0).getProductLevel());
            else
                bundle.putString("filterHeader", bmodel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());
            // bundle.putSerializable("serilizeContent",bmodel.brandMasterHelper.getBrandMaster());
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getChildLevelBo());

            if (bmodel.productHelper.getParentLevelBo() != null
                    && bmodel.productHelper.getParentLevelBo().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getParentLevelBo());
            } else {
                bundle.putBoolean("isFormBrand", false);
                bundle.putString("isFrom", "STK");
            }


            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    @Override
    public void updatebrandtext(String filtertext, int bid) {

        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = filtertext;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            // Clear the productName
            if (productName == null)
                productName = (TextView) findViewById(R.id.productName);
            productName.setText("");

            items = bmodel.productHelper.getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<ProductMasterBO>();
            mylist.clear();
            // Add the products into list
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);

                /**
                 * After scanning product,Barcode value stored in
                 * strBarCodeSearch Variable
                 */


                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || strBarCodeSearch.equals("ALL")) {


                    if (bid == -1 && ret.getIsSaleable() == 0 && ret.getIsReturnable() == 0) {
                        if (filtertext.equals("Brand")) {
                            mylist.add(ret);
                        }
                    } else if (bid == ret.getParentid()
                            && ret.getIsSaleable() == 0 && ret.getIsReturnable() == 0) {
                        mylist.add(ret);
                    }


                }


            }

            // set existing values
            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getmSampleProducts() != null) {
                for (ProductMasterBO bo : bmodel.getCounterSaleBO().getmSampleProducts()) {
                    for (ProductMasterBO productMasterBO : mylist) {
                        if (bo.getProductID().equals(productMasterBO.getProductID())) {
                            productMasterBO.setCsPiece(bo.getCsPiece());
                            productMasterBO.setCsCase(bo.getCsCase());
                            productMasterBO.setCsOuter(bo.getCsOuter());
                        }
                    }
                }
            }

            refreshList();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updategeneraltext(String filtertext) {
        generalbutton = filtertext;
        updatebrandtext(BRAND, -1);
    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername, List<Integer> filterid) {

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }

    @Override
    public void loadStartVisit() {

    }

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(CSFreeSample.this, R.layout.row_cs_sales, items);
        }

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(CSFreeSample.this, R.layout.row_cs_sales, items);
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
            View row = convertView;
            try {
                final ViewHolder holder;
                final ProductMasterBO counterBo = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.row_cs_sales, parent,
                            false);
                    holder = new ViewHolder();

                    holder.psname = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                    holder.pcsQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_pcs_qty);
                    holder.caseQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_case_qty);
                    holder.outerQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_outer_case_qty);
                   /* holder.txt_price = (TextView) row
                            .findViewById(R.id.txt_price);*/
                 //   holder.txt_price.setVisibility(View.GONE);

                    holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                  //  holder.txt_price.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));



                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                        holder.caseQty.setVisibility(View.GONE);

                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                        holder.pcsQty.setVisibility(View.GONE);

                    if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        holder.outerQty.setVisibility(View.GONE);


                    holder.pcsQty
                            .setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
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
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.caseQty
                            .setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.caseQty;
                                    QUANTITY.setTag(holder.counterSaleBO);
                                    int inType = holder.caseQty
                                            .getInputType();
                                    holder.caseQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.caseQty.onTouchEvent(event);
                                    holder.caseQty.setInputType(inType);
                                    holder.caseQty.selectAll();
                                    holder.caseQty.requestFocus();
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });
                    holder.outerQty
                            .setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.outerQty;
                                    QUANTITY.setTag(holder.counterSaleBO);
                                    int inType = holder.outerQty
                                            .getInputType();
                                    holder.outerQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.outerQty.onTouchEvent(event);
                                    holder.outerQty.setInputType(inType);
                                    holder.outerQty.selectAll();
                                    holder.outerQty.requestFocus();
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });


                    holder.pcsQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int pc_qty = SDUtil
                                                .convertToInt(qty);
                                        holder.counterSaleBO.setCsPiece(pc_qty);
                                        updateTotalLines();

                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                    // TODO Auto-generated method stub
                                }
                            });

                    holder.caseQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int ca_qty = SDUtil
                                                .convertToInt(qty);
                                        holder.counterSaleBO.setCsCase(ca_qty);
                                        updateTotalLines();

                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                    // TODO Auto-generated method stub
                                }
                            });

                    holder.outerQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int ou_qty = SDUtil
                                                .convertToInt(qty);
                                        holder.counterSaleBO.setCsOuter(ou_qty);
                                        updateTotalLines();

                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                    // TODO Auto-generated method stub
                                }
                            });


                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            productName.setText(holder.pname);
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);

                            if (viewFlipper.getDisplayedChild() != 0) {
                                viewFlipper.showPrevious();
                            }
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
                holder.caseQty.setText(holder.counterSaleBO.getCsCase() + "");
                holder.outerQty.setText(holder.counterSaleBO.getCsOuter() + "");


            } catch (Exception e) {
                Commons.print("" + e);
            }
            return (row);
        }
    }

    public class ViewHolder {
        private String productId, pname;
        private ProductMasterBO counterSaleBO;
        TextView psname;
        private EditText pcsQty, caseQty, outerQty;

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
                    getResources().getString(R.string.please_select_item), 0);
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

    private void updateTotalLines() {
        int count = 0;
        for (ProductMasterBO bo : mylist) {
            if (bo.getCsPiece() > 0 || bo.getCsCase() > 0 || bo.getCsOuter() > 0) {
                count += 1;
            }
        }
        txt_total_lines.setText(count + "");
    }

    private void getEnteredSampleProducts() {
        ArrayList<ProductMasterBO> lst = new ArrayList<>();
        boolean isData = false;
        for (ProductMasterBO bo : mylist) {
            if (bo.getCsPiece() > 0 || bo.getCsCase() > 0 || bo.getCsOuter() > 0) {
                lst.add(new ProductMasterBO(bo));

                bo.setCsOuter(0);
                bo.setCsCase(0);
                bo.setCsPiece(0);
                isData = true;
            }
        }

        if (isData) {
            if (bmodel.getCounterSaleBO() != null)
                bmodel.getCounterSaleBO().setmSampleProducts(lst);
            else {
                CounterSaleBO csBo = new CounterSaleBO();
                csBo.setmSampleProducts(lst);
                bmodel.setCounterSaleBO(csBo);
            }
        } else {
            if (bmodel.getCounterSaleBO() != null)
                bmodel.getCounterSaleBO().setmSampleProducts(null);
        }


    }

}
