package com.ivy.sd.png.view.van;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class VanStockAdjustFragment extends IvyBaseFragment implements BrandDialogInterface, View.OnClickListener,
        TextView.OnEditorActionListener {

    public static final String BRAND = "Brand";
    public static final String GENERAL = "General";
    private Vector<LoadManagementBO> vanunloadlist;
    private View view;
    private BusinessModel bmodel;
    private InputMethodManager inputManager;
    private TextView productName;
    private EditText QUANTITY, mEdt_searchproductName;
    private String append = "";
    private DrawerLayout mDrawerLayout;
    private String brandbutton, generalbutton;
    private ListView lvwplist;
    public boolean isSpecialFilter_enabled = true;
    boolean remarks_button_enable = true;
    boolean scheme_button_enable = true;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    int mSelectedLocationIndex;
    private ArrayList<ProductMasterBO> mylist;
    private Vector<ProductMasterBO> items;
    private Button mBtn_clear;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_actionbarwithfilter, container, false);
        // final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ll.addView(layoutInflater.inflate(
                R.layout.include_vanload_stock_adjustment_header, (ViewGroup) view, false));
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        ((VanStockAdjustActivity) getActivity()).getSupportActionBar();
        if (((VanStockAdjustActivity) getActivity()).getSupportActionBar() != null) {
            ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setIcon(null);
            // Used to on / off the back arrow icon
            ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final Intent intent = getActivity().getIntent();
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /*
                                                                                 * host
																				 * Activity
																				 */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (((VanStockAdjustActivity) getActivity()).getSupportActionBar() != null) {
                    ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setTitle(intent.getStringExtra("screentitle"));
                    getActivity().supportInvalidateOptionsMenu();
                }
            }

            public void onDrawerOpened(View drawerView) {
                if (((VanStockAdjustActivity) getActivity()).getSupportActionBar() != null) {
                    ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setTitle("Filter");
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        };
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (((VanStockAdjustActivity) getActivity()).getSupportActionBar() != null) {
            ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setIcon(null);
        }
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        vanunloadlist = bmodel.productHelper.getProducts();
        if (((VanStockAdjustActivity) getActivity()).getSupportActionBar() != null) {
            ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setTitle(intent.getStringExtra("screentitle"));
            ((VanStockAdjustActivity) getActivity()).getSupportActionBar().setIcon(null);
        }

        mDrawerLayout.closeDrawer(GravityCompat.END);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        lvwplist = (ListView) view.findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);
        view.findViewById(R.id.totalValue).setVisibility(View.GONE);
        view.findViewById(R.id.lcp).setVisibility(View.GONE);
        view.findViewById(R.id.distValue).setVisibility(View.GONE);
        view.findViewById(R.id.totalText).setVisibility(View.GONE);
        view.findViewById(R.id.lpc_title).setVisibility(View.GONE);
        view.findViewById(R.id.distText).setVisibility(View.GONE);
        view.findViewById(R.id.btn_search).setVisibility(View.GONE);
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.sihTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.sihTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.sihTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        mBtn_clear = (Button) view.findViewById(R.id.btn_clear);
        mBtn_clear.setOnClickListener(this);
        mBtn_clear.setOnEditorActionListener(this);
        mBtn_clear
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(),
                                    InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
        mEdt_searchproductName = (EditText) view.findViewById(R.id.edt_searchproductName);
        productName = (TextView) view.findViewById(R.id.productName);
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });
        mSelectedFilterMap.put("General", "Common");
        hideSpecialFilter();// hideSpecialFilter
        hideRemarksButton();
        hideShemeButton();
        updateBrandText("Brand", -1);
        // load location filter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * getActivity() would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
        }
    }

    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (vw == mBtn_clear) {
            mEdt_searchproductName.setText("");

            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");
            getActivity().supportInvalidateOptionsMenu();

            updateGeneralText(GENERAL);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
        // Change color if Filter is selected
        if (!GENERAL.equals(generalbutton))
            menu.findItem(R.id.menu_spl_filter).setIcon(
                    R.drawable.ic_action_star_select);
        if (!brandbutton.equals(BRAND))
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);

        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);

        if (isSpecialFilter_enabled)
            menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_spl_filter).setVisible(false);

        menu.findItem(R.id.menu_next).setVisible(!drawerOpen);

        if (remarks_button_enable)
            menu.findItem(R.id.menu_remarks).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_remarks).setVisible(false);

        if (scheme_button_enable)
            menu.findItem(R.id.menu_scheme).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_scheme).setVisible(false);

        menu.findItem(R.id.menu_apply_so).setVisible(false);
        menu.findItem(R.id.menu_apply_std_qty).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        else {
            if (bmodel.productHelper.getInStoreLocation().size() < 2)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
        }

        menu.findItem(R.id.menu_sih_apply).setVisible(false);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);

//        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
//            menu.findItem(R.id.menu_fivefilter).setVisible(true);
//        else
        menu.findItem(R.id.menu_product_filter).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else
                onBackButtonClick();
            return true;
        } else if (i == R.id.menu_next) {
            onNextButtonClick();
            return true;
        } else if (i == R.id.menu_spl_filter) {// generalFilterClicked();
            generalFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_product_filter) {// brandFilterClicked();
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackButtonClick() {
        startActivity(new Intent(getActivity(), HomeScreenActivity.class));
        getActivity().finish();
    }

    public void onNextButtonClick() {


        if (vanunloadlist.size() > 0) {
            new SaveVanStockAdjustment().execute();
        } else {
            mDialog1();
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                brandbutton = BRAND;
                generalbutton = GENERAL;
                getActivity().supportInvalidateOptionsMenu();
            }
            return true;
        }
        return false;
    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            String qty = s + append;
            QUANTITY.setText(qty);
        } else
            QUANTITY.setText(append);
    }

    private void generalFilterClickedFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", GENERAL);
            bundle.putBoolean("isFormBrand", false);
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void productFilterClickedFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            // To hide Key Board
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    mEdt_searchproductName.getWindowToken(),
                    InputMethodManager.RESULT_UNCHANGED_SHOWN);
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
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
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        dialog.dismiss();
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private void FiveFilterFragment() {
        try {

            QUANTITY = null;
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

    public void numberPressed(View vw) {
        Commons.print("number pressed");

        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER || bmodel.configurationMasterHelper.MUST_STOCK_ONLY)
            bmodel.showAlert(
                    getResources().getString(R.string.order_entry_not_allowed),
                    0);
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item),
                    0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String enterText = QUANTITY.getText().toString();
                String strQty;
                if (enterText.contains(".")) {
                    String[] splitValue = enterText.split("\\.");
                    try {

                        int s = SDUtil.convertToInt(splitValue[1]);
                        if (s == 0) {
                            s = SDUtil.convertToInt(splitValue[0]);
                            strQty = s + "";
                            QUANTITY.setText(strQty);
                        } else {
                            s = s / 10;
                            strQty = splitValue[0] + "." + s;
                            QUANTITY.setText(strQty);
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        Commons.printException("" + e);
                        strQty = SDUtil.convertToInt(enterText) + "";
                        QUANTITY.setText(strQty);
                    }


                } else {
                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    strQty = s + "";
                    QUANTITY.setText(strQty);
                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (!s.contains(".")) {
                    s = s + ".";
                    QUANTITY.setText(s);
                }
            } else {
                Button ed = (Button) view.findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // set the spl filter name on the button for display
        generalbutton = mFilterText;
        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
        // Close the drawer
        mDrawerLayout.closeDrawers();

        brandbutton = mFilterText;
        productName.setText("");
        items = getProducts();
        if (vanunloadlist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = vanunloadlist.size();
        ArrayList<LoadManagementBO> list = new ArrayList<>();

        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = vanunloadlist
                    .elementAt(i);
            if (bid == ret.getParentid() || bid == -1) {

                list.add(ret);
            }
        }

        MyAdapter mSchedule = new MyAdapter(list);
        lvwplist.setAdapter(mSchedule);
    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        mylist = new ArrayList<>();
        for (LevelBO levelBO : mParentIdList) {
            for (ProductMasterBO productBO : items) {
                if (productBO.getIsSaleable() == 1) {
                    if (levelBO.getProductID() == productBO.getParentid()) {
                        mylist.add(productBO);
                    }
                }
            }
        }
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mylist = new ArrayList<>();
        if (mAttributeProducts != null) {

            if (mParentIdList.size() > 0) {
                for (LevelBO levelBO : mParentIdList) {
                    for (ProductMasterBO productBO : items) {
                        if (productBO.getIsSaleable() == 1 && levelBO.getProductID() == productBO.getParentid()) {

                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(Integer.parseInt(productBO.getProductID()))) {
                                mylist.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {

                        if (pid == Integer.parseInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                            mylist.add(productBO);
                        }
                    }
                }
            }
        } else {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO productBO : items) {
                    if (productBO.getIsSaleable() == 1) {
                        if (levelBO.getProductID() == productBO.getParentid()) {
                            mylist.add(productBO);
                        }
                    }
                }
            }
        }
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {
        // TODO Auto-generated method stub

    }

    public Vector<ProductMasterBO> getProducts() {
        return bmodel.productHelper.getProductMaster();
    }

    public void hideSpecialFilter() {
        isSpecialFilter_enabled = false;
        generalbutton = "GENERAL";

    }

    public void hideRemarksButton() {
        remarks_button_enable = false;
    }

    public void hideShemeButton() {
        scheme_button_enable = false;
    }

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        ArrayList<LoadManagementBO> items;

        MyAdapter(ArrayList<LoadManagementBO> items) {
            super(getActivity(), R.layout.van_stock_adjust, items);
            this.items = items;
        }

        public LoadManagementBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;

            LoadManagementBO product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater
                        .inflate(R.layout.van_stock_adjust, parent, false);
                holder = new ViewHolder();
                holder.psname = (TextView) row.findViewById(R.id.productName);
                holder.sih = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_sih);
                holder.batchno = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_batchno);
                holder.adjustedSihEditText = (EditText) row
                        .findViewById(R.id.adjusted_sih);

                holder.adjustedSihEditText
                        .addTextChangedListener(new TextWatcher() {

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

                            @Override
                            public void afterTextChanged(Editable s) {
                                String qty = s.toString();
                                if (qty.length() > 0) {
                                    holder.productBO.setAdjusted_sih(SDUtil
                                            .convertToInt(qty));
                                    holder.productBO.calculateDifferenceInSih();
                                }
                            }

                        });

                holder.adjustedSihEditText
                        .setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView productName = (TextView) view.findViewById(R.id.productName);
                                productName.setText(holder.pname);
                                QUANTITY = holder.adjustedSihEditText;
                                int inType = holder.adjustedSihEditText
                                        .getInputType();
                                holder.adjustedSihEditText.onTouchEvent(event);
                                holder.adjustedSihEditText.setInputType(inType);
                                holder.adjustedSihEditText.selectAll();
                                holder.adjustedSihEditText.requestFocus();
                                inputManager.hideSoftInputFromWindow(
                                        mEdt_searchproductName.getWindowToken(),
                                        0);
                                return true;
                            }
                        });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.position = position;
            holder.productBO = product;

            holder.pname = product.getProductname();
            holder.psname.setText(product.getProductshortname());
            holder.sih.setText(String.valueOf(product.getStocksih()));
            holder.batchno.setText(String.valueOf(product.getBatchNo()));
            holder.adjustedSihEditText.setText(String.valueOf(product.getAdjusted_sih()));
            return (row);
        }
    }

    class ViewHolder {
        LoadManagementBO productBO;
        int position;
        String pname;
        TextView psname, sih, batchno;
        EditText adjustedSihEditText;
    }

    class SaveVanStockAdjustment extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.vanunloadmodulehelper
                        .saveVanStockAdjustment(vanunloadlist);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            onCreateDialog();
        }

    }

    protected Dialog onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(null)
                .setCancelable(false)
                .setTitle(
                        getResources().getString(
                                R.string.saved_successfully))
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                getActivity().finish();
                                Intent intent = new Intent(
                                        getActivity(),
                                        HomeScreenActivity.class);
                                startActivity(intent);
                            }
                        });
        bmodel.applyAlertDialogTheme(builder);
        return null;

    }

    public void mDialog1() {
        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder1
                .setIcon(null)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.no_items_added))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                            }
                        });

        AlertDialog alertDialog1 = alertDialogBuilder1.create();

        alertDialog1.show();
    }
}
