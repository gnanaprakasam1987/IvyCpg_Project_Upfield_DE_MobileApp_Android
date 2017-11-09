package com.ivy.countersales;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ApplyBo;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
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
public class CSapply extends IvyBaseActivityNoActionBar implements BrandDialogInterface, View.OnClickListener {
    AutoCompleteTextView feedBackAutoCompleteTV, productsAutoCompleteTV;
    ArrayAdapter<ProductMasterBO> spnAdapter;
    ArrayAdapter<StandardListBO> spnFeedAdapter;
    BusinessModel bmodel;
    ArrayList<ProductMasterBO> lstProducts;
    ArrayList<StandardListBO> lstFeedBacks;
    HashMap<String, String> productNameMap = new HashMap<>();
    int mSelectedProductid = 0;
    String feedBack = "";
    EditText edt_result, edt_feedback, edt_hour, edt_minute;
    private EditText QUANTITY;
    private DrawerLayout mDrawerLayout;
    private static final String BRAND = "Brand";
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    public String brandbutton, generalbutton;
    private TextView productName;
    public Vector<ProductMasterBO> items;
    private static final String GENERAL = "General";
    private Toolbar toolbar;
    private FullLengthListView lvwplist;
    ActionBarDrawerToggle mDrawerToggle;
    private Button cancelBtn, applyBtn, saveBtn, deletebtn;
    private ArrayList<ApplyBo> tempTestList;
    private boolean isTested = false;

    //    public ArrayList<ProductMasterBO> mylist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        setContentView(R.layout.cs_apply);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        bmodel.mSelectedActivityName = "Apply/Test";
        setScreenTitle(bmodel.mSelectedActivityName);
        productsAutoCompleteTV = (AutoCompleteTextView) findViewById(R.id.autoCompleteTv_products);
        feedBackAutoCompleteTV = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        lstProducts = new ArrayList<>();
        lstFeedBacks = new ArrayList<>();
        tempTestList = new ArrayList<>();

        // lstProducts.addAll(bmodel.productHelper.getProductMaster());

        productsAutoCompleteTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                productsAutoCompleteTV.showDropDown();
                return false;
            }
        });

        productsAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

                ProductMasterBO itembo = (ProductMasterBO) parent.getItemAtPosition(pos);
                mSelectedProductid = Integer.parseInt(itembo.getProductID());

                if (mSelectedProductid > 0) {
                    isEditable(true);
                    updateValue();
                } else {
                    mSelectedProductid = 0;
                    isEditable(false);
                }
            }

        });


        productsAutoCompleteTV.setOnFocusChangeListener(new View.OnFocusChangeListener()

        {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && productsAutoCompleteTV.getText().toString().length() == 0
                        && productsAutoCompleteTV.getText().toString().isEmpty())
                    isEditable(false);
            }
        });
        edt_result = (EditText)

                findViewById(R.id.edt_result);

        edt_hour = (EditText)

                findViewById(R.id.edt_hour);

        edt_minute = (EditText)

                findViewById(R.id.edt_minute);

        lvwplist = (FullLengthListView)

                findViewById(R.id.lvwplist);

        edt_result.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edt_hour.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edt_minute.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        mDrawerLayout = (DrawerLayout)

                findViewById(
                        R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerToggle = new

                ActionBarDrawerToggle(this, /* host Activity */
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
        // mDrawerLayout.closeDrawer(GravityCompat.END);


        feedBackAutoCompleteTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                feedBackAutoCompleteTV.showDropDown();

                return false;
            }
        });

        feedBackAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    feedBack = lstFeedBacks.get(position).getListName();
                } else {
                    feedBack = getResources().getString(R.string.select);
                }
            }
        });

        if (bmodel.getCounterSaleBO() != null) {
            tempTestList = new ArrayList<>();
            tempTestList = bmodel.getCounterSaleBO().getmTestProducts();
            MyAdapter mAdapter = new MyAdapter(tempTestList);
            lvwplist.setAdapter(mAdapter);
        }

        cancelBtn = (Button) findViewById(R.id.cancelbtn);
        cancelBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        applyBtn = (Button) findViewById(R.id.applybtn);
        applyBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        saveBtn = (Button) findViewById(R.id.btn_save);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        deletebtn = (Button) findViewById(R.id.btn_delete);
        deletebtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        cancelBtn.setOnClickListener(this);
        applyBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        deletebtn.setOnClickListener(this);

        ((TextView) findViewById(R.id.tv_product_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.txt_timetaken)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.txt_result)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.txt_feedback)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvProductNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.timeTakenTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.resultTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.feedbackTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_counter_sales, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_barcode).setVisible(true);
//        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
//            menu.findItem(R.id.menu_fivefilter).setVisible(true);
//        else
//            menu.findItem(R.id.menu_product_filter).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                //setValues();
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.menu_product_filter) {
            productFilterClickedFragment();
            supportInvalidateOptionsMenu();
            return true;
        } else if (item.getItemId() == R.id.menu_barcode) {
            checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                new IntentIntegrator(this).setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
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

    private boolean isValidTimeProvided() {

        if (!edt_hour.getText().toString().equals("") && !edt_minute.getText().toString().equals("")) {
            if (Integer.parseInt(edt_hour.getText().toString()) <= 12 && Integer.parseInt(edt_hour.getText().toString()) <= 59)
                return true;
        }
        return false;
    }

    @Override
    public void updategeneraltext(String filtertext) {
        generalbutton = filtertext;
        updatebrandtext(BRAND, -1);
    }

    @Override
    public void updatebrandtext(String filtertext, int bid) {
        // TODO Auto-generated method stub

        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();
            // Change the Brand button Name
            brandbutton = filtertext;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            lstProducts.clear();
            ProductMasterBO bo = new ProductMasterBO();
            bo.setProductID("0");
            bo.setProductName(getResources().getString(R.string.select));
            lstProducts.add(new ProductMasterBO(bo));
            productNameMap.put(bo.getProductID(), bo.getProductName());

            items = bmodel.productHelper.getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);

            // Add the products into list
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
                if (((bid == -1 || bid == ret.getParentid()) && ret.getCsTestSIH() > 0) && ret.getIsSaleable() == 1) {
                    lstProducts.add(ret);
                    productNameMap.put(ret.getProductID(), ret.getProductName());
                }
            }
            loadProducts();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void loadProducts() {
        spnAdapter = new ArrayAdapter<ProductMasterBO>(this, R.layout.autocompelete_bluetext_layout, lstProducts);
        spnAdapter
                .setDropDownViewResource(R.layout.autocomplete_bluetext_list_item);


        productsAutoCompleteTV.setAdapter(spnAdapter);
        productsAutoCompleteTV.setThreshold(1);
        productsAutoCompleteTV.setSelection(0);

        if (bmodel.CS_StockApplyHelper.loadFeedBakcs() != null)
            lstFeedBacks = bmodel.CS_StockApplyHelper.loadFeedBakcs();
        StandardListBO selectBo = new StandardListBO();
        selectBo.setListID("0");
        selectBo.setListName(getResources().getString(R.string.select));
        lstFeedBacks.add(0, selectBo);
        spnFeedAdapter = new ArrayAdapter<StandardListBO>(this, R.layout.autocompelete_bluetext_layout, lstFeedBacks);
        spnFeedAdapter
                .setDropDownViewResource(R.layout.autocomplete_bluetext_list_item);
        feedBackAutoCompleteTV.setAdapter(spnFeedAdapter);
        feedBackAutoCompleteTV.setThreshold(1);
        feedBackAutoCompleteTV.setSelection(0);
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

    private void addValues() {
        int testHour = 0, testMinute = 0;
        if (!edt_hour.getText().toString().equals("") && edt_hour.getText() != null && !edt_hour.getText().toString().equalsIgnoreCase("null"))
            testHour = Integer.parseInt(edt_hour.getText().toString());
        if (!edt_minute.getText().toString().equals("") && edt_minute.getText() != null && !edt_minute.getText().toString().equalsIgnoreCase("null"))
            testMinute = Integer.parseInt(edt_minute.getText().toString());

        String result = edt_result.getText().toString();
        /*StandardListBO feedBo = (StandardListBO) spnFeedBack.getSelectedItem();
        String feedback = feedBo.getListName();*/
        boolean isModifed = false;
        if (mSelectedProductid > 0 && (!edt_hour.getText().toString().isEmpty()
                || !edt_minute.getText().toString().isEmpty())) {

            if (tempTestList.size() == 0) {
                ApplyBo applyBo = new ApplyBo();
                applyBo.setTestFeedback(feedBack.equals(getResources().getString(R.string.select)) ? "" : feedBack);
                applyBo.setTestedProductId(mSelectedProductid);
                applyBo.setResult(result);
                applyBo.setTesthour(testHour);
                applyBo.setTestTime(testMinute);
                tempTestList.add(applyBo);
            } else {
                for (int i = 0; i < tempTestList.size(); i++) {
                    ApplyBo aBo = tempTestList.get(i);
                    if (aBo.getTestedProductId() == mSelectedProductid) {
                        aBo.setTestFeedback(feedBack.equals(getResources().getString(R.string.select)) ? "" : feedBack);
                        aBo.setTestedProductId(mSelectedProductid);
                        aBo.setResult(result);
                        aBo.setTesthour(testHour);
                        aBo.setTestTime(testMinute);
                        tempTestList.remove(i);
                        tempTestList.add(i, aBo);
                        isModifed = true;
                        break;
                    }
                }
                if (!isModifed) {
                    ApplyBo applyBo = new ApplyBo();
                    applyBo.setTestFeedback(feedBack.equals(getResources().getString(R.string.select)) ? "" : feedBack);
                    applyBo.setTestedProductId(mSelectedProductid);
                    applyBo.setResult(result);
                    applyBo.setTesthour(testHour);
                    applyBo.setTestTime(testMinute);
                    tempTestList.add(applyBo);
                }
            }
            if (tempTestList.size() > 0) {
                MyAdapter mAdapter = new MyAdapter(tempTestList);
                lvwplist.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                clearValues();
            }
        } else
            Toast.makeText(CSapply.this, "Enter Time Taken ", Toast.LENGTH_SHORT).show();

    }

    private void deleteValues() {

        String result = edt_result.getText().toString();
        /*StandardListBO feedBackBo = (StandardListBO) spnFeedBack.getSelectedItem();
        String feedback = feedBackBo.getListName();*/

        if (mSelectedProductid > 0 || !result.equals("") ||
                !feedBack.equals(getResources().getString(R.string.select))) {
            clearValues();

            for (int i = 0; i < tempTestList.size(); i++) {
                ApplyBo aBo = tempTestList.get(i);
                if (aBo.getTestedProductId() == mSelectedProductid) {
                    tempTestList.remove(i);

                    break;
                }
            }
        } else {
            Toast.makeText(CSapply.this, "No Data to delete", Toast.LENGTH_SHORT).show();
        }

        MyAdapter mAdapter = new MyAdapter(tempTestList);
        lvwplist.setAdapter(mAdapter);

    }

    private void clearValues() {
        mSelectedProductid = 0;
        feedBack = getResources().getString(R.string.select);
        edt_result.setText("");
        feedBackAutoCompleteTV.setText("");
        edt_hour.setText("");
        edt_minute.setText("");
        productsAutoCompleteTV.setText("");

    }

    private void updateValue(ApplyBo applyBo) {
        if (applyBo != null) {
            edt_result.setText(applyBo.getResult());
            if (!applyBo.getTestFeedback().isEmpty())
                feedBackAutoCompleteTV.setText(lstFeedBacks.get(bmodel.mCounterSalesHelper.getItemFeedBackIndex(applyBo.getTestFeedback(), lstFeedBacks)).toString());
            else
                feedBackAutoCompleteTV.setText("");

            if (applyBo.getTesthour() > 0)
                edt_hour.setText(applyBo.getTesthour() + "");
            if (applyBo.getTestTime() > 0)
                edt_minute.setText(applyBo.getTestTime() + "");
            mSelectedProductid = applyBo.getTestedProductId();
            if (mSelectedProductid > 0) {
                productsAutoCompleteTV.setText(lstProducts.get(bmodel.mCounterSalesHelper.getItemIndex(mSelectedProductid, lstProducts)).toString());
            }
        }
    }

    private void updateValue() {
        if (mSelectedProductid != 0) {
            if (tempTestList.size() > 0) {
                for (ApplyBo applyBo : tempTestList) {
                    if (mSelectedProductid == applyBo.getTestedProductId()) {
                        isTested = true;
                        edt_result.setText(applyBo.getResult());
                        if (!applyBo.getTestFeedback().isEmpty())
                            feedBackAutoCompleteTV.setText(lstFeedBacks.get(bmodel.mCounterSalesHelper.getItemFeedBackIndex(applyBo.getTestFeedback(), lstFeedBacks)).toString());
                        else
                            feedBackAutoCompleteTV.setText("");
                        if (applyBo.getTesthour() > 0)
                            edt_hour.setText(applyBo.getTesthour() + "");
                        if (applyBo.getTestTime() > 0)
                            edt_minute.setText(applyBo.getTestTime() + "");
                        break;
                    } else {
                        isTested = false;
                    }
                }
                if (!isTested) {
                    edt_hour.setText("");
                    edt_minute.setText("");
                    edt_result.setText("");
                    feedBackAutoCompleteTV.setText("");
                }

            }
        }
    }


    private void setValues() {

        if (bmodel.getCounterSaleBO() != null && tempTestList.size() > 0) {
            bmodel.getCounterSaleBO().setmTestProducts(tempTestList);
        } else if (tempTestList.size() > 0) {
            CounterSaleBO counterSaleBO = new CounterSaleBO();
            counterSaleBO.setmTestProducts(tempTestList);
            bmodel.setCounterSaleBO(counterSaleBO);
        }
    }

    private void isEditable(boolean flag) {
        edt_hour.setEnabled(flag);
        edt_minute.setEnabled(flag);
        edt_result.setEnabled(flag);
        //edt_feedback.setEnabled(flag);
        feedBackAutoCompleteTV.setEnabled(flag);
        saveBtn.setEnabled(flag);
        deletebtn.setEnabled(flag);
    }

    @Override
    public void onClick(View v) {
        Button id = (Button) v;
        if (id == applyBtn) {
            setValues();
            finish();
        } else if (id == cancelBtn) {
            finish();
        } else if (id == saveBtn) {
            addValues();
        } else if (id == deletebtn)
            deleteValues();
    }

    private class MyAdapter extends BaseAdapter {
        private final ArrayList<ApplyBo> items;

        /**
         * One arg constructor method
         *
         * @param --ArrayList <AssetTrackingBO>
         */

        public MyAdapter(ArrayList<ApplyBo> items) {
            super();
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_csapply, parent, false);

                row.setTag(holder);

                holder.tvProductName = (TextView) row
                        .findViewById(R.id.tvProductName);
                holder.tvtimeTaken = (TextView) row
                        .findViewById(R.id.tvtimeTaken);
                holder.tvresult = (TextView) row
                        .findViewById(R.id.tvresult);
                holder.tvfeedback = (TextView) row
                        .findViewById(R.id.tvfeedback);

                holder.tvProductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tvtimeTaken.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tvresult.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tvfeedback.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.applyBo = items.get(position);

            holder.tvProductName.setText(productNameMap.get("" + holder.applyBo.getTestedProductId()));
            holder.tvtimeTaken.setText(holder.applyBo.getTesthour() + ":" + holder.applyBo.getTestTime());
            holder.tvresult.setText(holder.applyBo.getResult());
            holder.tvfeedback.setText(holder.applyBo.getTestFeedback());

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateValue(holder.applyBo);
                }
            });

            return row;
        }
    }

    class ViewHolder {
        ApplyBo applyBo;
        TextView tvProductName;
        TextView tvtimeTaken;
        TextView tvresult;
        TextView tvfeedback;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                String barcode = result.getContents().toString();
                if (barcode != null && !"".equals(barcode)) {
                    for (ProductMasterBO productMasterBO : bmodel.productHelper.getProductMaster()) {
                        if (productMasterBO.getBarCode().equals(barcode)) {
                            int index = bmodel.mCounterSalesHelper.getItemIndex(Integer.parseInt(productMasterBO.getProductID()), lstProducts);
                            if (index != -1)
                                productsAutoCompleteTV.setText(lstProducts.get(index).toString());
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
