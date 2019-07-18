package com.ivy.cpg.primarysale.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by vinodh.r on 21-09-2015.
 */
public class PrimarySaleStockCheckFragment extends IvyBaseFragment implements
        TextView.OnEditorActionListener,
        BrandDialogInterface, View.OnClickListener, FiveLevelFilterCallBack {


    public ArrayList<ProductMasterBO> stockSkuList;
    public HashMap<Integer, Integer> mSelectedIdByLevelId;
    private View view;
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> items;
    private ArrayList<ProductMasterBO> mylist;
    private ListView distStockCheckListView;
    private TextView productName;
    private TextView shelf_case, shelf_piece, shelf_outer;
    private EditText QUANTITY, mEdt_searchproductName;
    private InputMethodManager inputManager;
    private String append = "";
    private ListViewAdapter mSchedule;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewFlipper viewFlipper;
    private Button mBtn_Search, mBtnFilterPopup, mBtn_clear;
    private ArrayList<String> mSearchTypeArray = new ArrayList<String>();
    private String mSelectedFilter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_distributor_stock_check,
                container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        mBtn_Search = (Button) view.findViewById(R.id.btn_search);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        mEdt_searchproductName = (EditText) view.findViewById(R.id.edt_searchproductName);
        productName = (TextView) view.findViewById(R.id.productName);
        mBtnFilterPopup = (Button) view.findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) view.findViewById(R.id.btn_clear);

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mSearchTypeArray = new ArrayList<String>();
        // mSearchTypeArray.add(getResources().getString(R.string.product_short_name));
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        mEdt_searchproductName.setOnEditorActionListener(this);

        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

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
                    // TODO Auto-generated method stub

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    // TODO Auto-generated method stub

                }
            });
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public void onStart() {
        super.onStart();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        FrameLayout drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        setScreenTitle(bmodel.mSelectedActivityName);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(
                null);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                setScreenTitle(bmodel.mSelectedActivityName);
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {

                setScreenTitle(getResources().getString(R.string.filter));
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        items = bmodel.productHelper.getProductMaster();
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        distStockCheckListView = (ListView) view.findViewById(R.id.dist_stock_check_listview);
        productName = (TextView) view.findViewById(R.id.productName);
        shelf_case = (TextView) view.findViewById(R.id.shelfCaseTitle);
        shelf_outer = (TextView) view.findViewById(R.id.shelfOuterTitle);
        shelf_piece = (TextView) view.findViewById(R.id.shelfPcsTitle);

        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.productNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        shelf_case.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        shelf_piece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        shelf_outer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdt_searchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        if (bmodel.configurationMasterHelper.SHOW_SC)
            shelf_case.setVisibility(View.VISIBLE);
        if (bmodel.configurationMasterHelper.SHOW_SHO)
            shelf_outer.setVisibility(View.VISIBLE);
        if (bmodel.configurationMasterHelper.SHOW_SP)
            shelf_piece.setVisibility(View.VISIBLE);

        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });
//        mEdt_searchproductName = (EditText) view.findViewById(R.id.edt_searchproductName);
//        mEdt_searchproductName.setOnEditorActionListener(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                bmodel.mSelectedActivityName);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(
                null);
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = items.size();
        stockSkuList = new ArrayList<ProductMasterBO>();
        stockSkuList.clear();
        for (int j = 0; j < siz; j++) {
            stockSkuList.add(items.get(j));
        }
        refreshList();
    }

    public void loadSearchedList() {

        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            mylist = new ArrayList<>();
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);

                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.prod_code))) {
                    if ((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString().toLowerCase())))
                        mylist.add(ret);

                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.product_name))) {
                    if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                } else {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                    else if ((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString().toLowerCase())))
                        mylist.add(ret);
                    else if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                }
                /*
                 * else if (mSelectedFilter.equals(getResources().getString(
                 * R.string.product_short_name))) { if
                 * (ret.getProductShortName() .toLowerCase() .contains(
                 * mEdt_searchproductName.getText().toString() .toLowerCase()))
                 * { mylist.add(ret); } }
                 */
            }


            mSchedule = new ListViewAdapter(mylist);
            distStockCheckListView.setAdapter(mSchedule);
        } else {
            Toast.makeText(getActivity(), "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void refreshList() {
        mSchedule = new ListViewAdapter(stockSkuList);
        distStockCheckListView.setAdapter(mSchedule);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            bmodel.distTimeStampHeaderHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
            getActivity().setResult(getActivity().RESULT_OK);
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_next) {
            if (bmodel.distributorMasterHelper.hasDistributorStockCheck()) {
                new SaveAsyncTask().execute();
            } else {
                mDialog1();
            }
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        try {
            menu.findItem(R.id.menu_remarks).setVisible(false);
            menu.findItem(R.id.menu_spl_filter).setVisible(false);
            menu.findItem(R.id.menu_remarks).setVisible(false);
            menu.findItem(R.id.menu_scheme).setVisible(false);
            menu.findItem(R.id.menu_apply_so).setVisible(false);
            menu.findItem(R.id.menu_apply_std_qty).setVisible(false);
            menu.findItem(R.id.menu_sih_apply).setVisible(false);
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
            //  if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
            menu.findItem(R.id.menu_next).setVisible(true);
            // return super.onPrepareOptionsMenu(menu);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    public void mDialog1() {
        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder1
                .setIcon(null)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.no_products_exists))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                            }
                        });

        AlertDialog alertDialog1 = alertDialogBuilder1.create();

        alertDialog1.show();
    }

    public void numberPressed(View vw) {
        int val = 0;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                /*
                 * int s = SDUtil.convertToInt((String) QUANTITY.getText()
                 * .toString()); s = s / 10; QUANTITY.setText(s + ""); val = s;
                 */

                String s = QUANTITY.getText().toString();
                if (s != null) {
                    if (!(s.length() == 0)) {
                        s = s.substring(0, s.length() - 1);
                        if (s.length() == 0) {
                            s = "0";
                        }
                    }
                    QUANTITY.setText(s);
                }
            } else {
                Button ed = (Button) getView().findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt((String) append);
            }
        }
    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {

    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        stockSkuList = new ArrayList<>();
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (mAttributeProducts != null) {
            if (mFilteredPid != 0) {
                for (ProductMasterBO productBO : items) {
                    if (productBO.getIsSaleable() == 1 && productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                        if (mAttributeProducts.contains(SDUtil.convertToInt(productBO.getProductID()))) {
                            stockSkuList.add(productBO);
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {

                        if (pid == SDUtil.convertToInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                            stockSkuList.add(productBO);
                        }
                    }
                }
            }
        } else {

            for (ProductMasterBO productBO : items) {
                if (productBO.getIsSaleable() == 1) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        stockSkuList.add(productBO);
                    }
                }
            }
        }

        mSchedule = new ListViewAdapter(stockSkuList);
        distStockCheckListView.setAdapter(mSchedule);
        // strBarCodeSearch = "ALL";
        // updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
    }

    private void FiveFilterFragment() {
        try {
            // QUANTITY = null;
            Vector<String> vect = new Vector();
            for (String string : getResources().getStringArray(
                    R.array.productFilterArray)) {
                vect.add(string);
            }

            mDrawerLayout.openDrawer(GravityCompat.END);

            FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<Object>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
//        bmodel = (BusinessModel) getAc();
//        bmodel.setContext(this);
        if (vw == mBtn_Search) {
            // viewFlipper.setInAnimation(this, R.anim.in_from_left);
            // viewFlipper.setOutAnimation(this, R.anim.out_to_right);
            viewFlipper.showNext();
        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.select_dialog_singlechoice,
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
            productName.setText("");
            refreshList();
            viewFlipper.showPrevious();

        }
    }

    class ListViewAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;


        public ListViewAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.fragment_distributor_stock_check_listview, items);
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
                final ProductMasterBO product = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.fragment_distributor_stock_check_listview, parent,
                            false);
                    holder = new ViewHolder();
                    holder.psname = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.shelfPcsQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_sp_qty);
                    holder.shelfCaseQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_sc_qty);
                    holder.shelfOuterQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_shelfouter_qty);

                    holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfPcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfCaseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfOuterQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                    if (bmodel.configurationMasterHelper.SHOW_SC)
                        holder.shelfCaseQty.setVisibility(View.VISIBLE);
                    if (bmodel.configurationMasterHelper.SHOW_SHO)
                        holder.shelfOuterQty.setVisibility(View.VISIBLE);
                    if (bmodel.configurationMasterHelper.SHOW_SP)
                        holder.shelfPcsQty.setVisibility(View.VISIBLE);

                    holder.shelfPcsQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (qty.length() > 0)
                                        holder.shelfPcsQty.setSelection(qty.length());
                                    if (!qty.equals("")) {
                                        int sp_qty = SDUtil
                                                .convertToInt(holder.shelfPcsQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(0)
                                                .setShelfPiece(sp_qty);
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

                    holder.shelfCaseQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (qty.length() > 0)
                                        holder.shelfCaseQty.setSelection(qty.length());
                                    if (!qty.equals("")) {
                                        int scqty = SDUtil
                                                .convertToInt(holder.shelfCaseQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(0)
                                                .setShelfCase(scqty);
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

                    holder.shelfOuterQty.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.shelfOuterQty.setSelection(qty.length());

                            if (!qty.equals("")) {
                                int shelfoqty = SDUtil
                                        .convertToInt(holder.shelfOuterQty
                                                .getText().toString());
                                holder.productObj.getLocations()
                                        .get(0)
                                        .setShelfOuter(shelfoqty);
                            }

                        }
                    });

                    holder.shelfPcsQty
                            .setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.shelfPcsQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.shelfPcsQty
                                            .getInputType();
                                    holder.shelfPcsQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.shelfPcsQty.onTouchEvent(event);
                                    holder.shelfPcsQty.setInputType(inType);
                                    holder.shelfPcsQty.requestFocus();
                                    if (holder.shelfPcsQty.getText().length() > 0)
                                        holder.shelfPcsQty.setSelection(holder.shelfPcsQty.getText().length());
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfCaseQty
                            .setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.shelfCaseQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.shelfCaseQty
                                            .getInputType();
                                    holder.shelfCaseQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.shelfCaseQty.onTouchEvent(event);
                                    holder.shelfCaseQty.setInputType(inType);
                                    holder.shelfCaseQty.requestFocus();
                                    if (holder.shelfCaseQty.getText().length() > 0)
                                        holder.shelfCaseQty.setSelection(holder.shelfCaseQty.getText().length());
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfOuterQty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            productName.setText(holder.pname);
                            QUANTITY = holder.shelfOuterQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.shelfOuterQty.getInputType();
                            holder.shelfOuterQty.setInputType(InputType.TYPE_NULL);
                            holder.shelfOuterQty.onTouchEvent(event);
                            holder.shelfOuterQty.setInputType(inType);
                            holder.shelfOuterQty.requestFocus();
                            if (holder.shelfOuterQty.getText().length() > 0)
                                holder.shelfOuterQty.setSelection(holder.shelfOuterQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });

                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            productName.setText(holder.pname);
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);

                        }
                    });


                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.productObj = product;

                holder.productId = holder.productObj.getProductID();
                holder.pname = holder.productObj.getProductName();

                holder.psname.setText(holder.productObj.getProductShortName());

                if (holder.productObj.getLocations()
                        .get(0).getShelfPiece() >= 0) {
                    String strShelfPiece = holder.productObj.getLocations()
                            .get(0).getShelfPiece()
                            + "";
                    holder.shelfPcsQty.setText(strShelfPiece.equals("0") ? "" : strShelfPiece);
                } else {
                    holder.shelfPcsQty.setText("");
                }
                if (holder.productObj.getLocations()
                        .get(0).getShelfCase() >= 0) {
                    String strShelfCase = holder.productObj.getLocations()
                            .get(0).getShelfCase()
                            + "";
                    holder.shelfCaseQty.setText(strShelfCase.equals("0") ? "" : strShelfCase);
                } else {
                    holder.shelfCaseQty.setText("");
                }

                if (holder.productObj.getLocations()
                        .get(0).getShelfOuter() >= 0) {
                    String strShelfPiece = holder.productObj.getLocations()
                            .get(0).getShelfOuter()
                            + "";
                    holder.shelfOuterQty.setText(strShelfPiece.equals("0") ? "" : strShelfPiece);
                } else {
                    holder.shelfOuterQty.setText("");
                }
                TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
                if (position % 2 == 0) {
                    row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
                } else {
                    row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
                }


                if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped())
                    holder.shelfPcsQty.setEnabled(false);
                else
                    holder.shelfPcsQty.setEnabled(true);

                if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped())
                    holder.shelfCaseQty.setEnabled(false);
                else
                    holder.shelfCaseQty.setEnabled(true);

                if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped())
                    holder.shelfOuterQty.setEnabled(false);
                else
                    holder.shelfOuterQty.setEnabled(true);


            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return (row);
        }
    }

    public class ViewHolder {
        private String productId, pname;
        private ProductMasterBO productObj;
        private TextView psname;
        private EditText shelfPcsQty, shelfCaseQty, shelfOuterQty;
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                bmodel.distributorMasterHelper.saveDistributorClosingStock();
                bmodel.productHelper.clearOrderTable();
                // Upadte isVisited Flag
                //bmodel.updateIsVisitedFlag();


                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {

//                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
//                        .now(SDUtil.TIME));
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

}
