package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajesh.k on 07-09-2015.
 */
public class CurrentStockBatchViewFragment extends IvyBaseFragment implements BrandDialogInterface {
    private BusinessModel bmodel;
    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Vector<LoadManagementBO> mylist;
    public ViewFlipper viewFlipper;
    public ListView lvwplist;
    public HashMap<Integer, Integer> mSelectedIdByLevelId;
    private static final String BRAND = "Brand";
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private View view;

    @Nullable
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        setHasOptionsMenu(true);
        /*
         * if (bmodel.configurationMasterHelper.SELECT_DEFAULT_FILTER) {
		 * selectByDefaultFirstFilter(); } else { if
		 * (bmodel.mPriceChangeCheckHelper.mSelectedFilter == 0)
		 * bmodel.mPriceChangeCheckHelper.mSelectedFilter = -1; if
		 * (bmodel.mPriceChangeCheckHelper.mSelectedParentFilter == 0)
		 * bmodel.mPriceChangeCheckHelper.mSelectedParentFilter = -1; }
		 */

        if (bmodel.mPriceTrackingHelper.mSelectedFilter == 0)
            bmodel.mPriceTrackingHelper.mSelectedFilter = -1;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_actionbarwithfilter, container,
                false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                "Report");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(
                R.drawable.icon_stock);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                ((AppCompatActivity) getActivity()).getSupportActionBar()
                        .setTitle("Report");
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {

                ((AppCompatActivity) getActivity()).getSupportActionBar()
                        .setTitle(getResources().getString(R.string.filter));
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        getView().findViewById(R.id.layout_sih).setVisibility(View.VISIBLE);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        LinearLayout ll = (LinearLayout) getView().findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ll.addView(layoutInflater.inflate(
                R.layout.current_stock_batchwise_layout, null));
        getView().findViewById(R.id.keypad).setVisibility(View.GONE);


        ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(R.id.view_flipper);
        viewFlipper.setVisibility(View.INVISIBLE);

        try {
            if (bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                    R.id.sihtitle).getTag()) != null)
                ((TextView) getView().findViewById(R.id.sihtitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(getView().findViewById(R.id.sihtitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
            getView().findViewById(R.id.sih_cs_title).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.sih_ou_title).setVisibility(View.VISIBLE);
        }


        viewFlipper = (ViewFlipper) getView().findViewById(R.id.view_flipper);

        RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(R.id.stockcheckroot);
        relativeLayout.setVisibility(View.GONE);


        bmodel.reportHelper.updateBaseUOM("ORDER", 3);


        lvwplist = (ListView) getView().findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);

        /** Load products from product master **/
        LoadManagementBO lbo;
        mylist = new Vector<LoadManagementBO>();
        for (int j = 0; j < bmodel.productHelper.getProducts().size(); j++) {
            lbo = bmodel.productHelper.getProducts().get(j);
            if (lbo.getStocksih() > 0)
                mylist.add(lbo);
        }
        updateBrandText("Brand", -1);

        updateTotalSIHValue();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);


        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);

        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_scheme).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        menu.findItem(R.id.menu_sih_apply).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        else
            menu.findItem(R.id.menu_product_filter).setVisible(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            fiveFilterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName, List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

// Close the drawer
        mDrawerLayout.closeDrawers();

//        brandbutton=filtertext;

        int brandid = bid;
//        productName.setText("");
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        ArrayList<LoadManagementBO> temp = new ArrayList<LoadManagementBO>();
        for (int i = 0; i < siz; ++i) {

            LoadManagementBO ret = mylist.get(i);
            if (brandid == -1 || brandid == ret.getParentid()) {
                temp.add(ret);

            }
        }
        MyAdapter mSchedule = new MyAdapter(temp);
        lvwplist.setAdapter(mSchedule);


    }

    private LoadManagementBO product;

    private class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        private ArrayList<LoadManagementBO> items;

        public MyAdapter(ArrayList<LoadManagementBO> items) {
            super(getActivity(),
                    R.layout.row_batchwise_current_stock_report, items);
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

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            product = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_batchwise_current_stock_report, parent,
                        false);
                holder = new ViewHolder();

                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.sih = (TextView) row.findViewById(R.id.sih);
                holder.batchnumber = (TextView) row
                        .findViewById(R.id.batchnumber);
                holder.sih_cs = (TextView) row.findViewById(R.id.sih_cs);
                holder.sih_ou = (TextView) row.findViewById(R.id.sih_ou);

                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    holder.sih_cs.setVisibility(View.VISIBLE);
                    holder.sih_ou.setVisibility(View.VISIBLE);
                }

                // inputManager.hideSoftInputFromWindow(
                // mEdt_searchproductName.getWindowToken(), 0);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.ref = position;
            holder.psname.setText(product.getProductshortname());
            holder.pname = product.getProductname();

            if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                boolean isUomWiseSplitted = false;
                int rem_sih = 0;
                int totalQty = product.getStocksih();

                if (product.isBaseUomCaseWise() && product.getCaseSize() != 0) {
                    isUomWiseSplitted = true;

                    holder.sih_cs.setText(totalQty / product.getCaseSize() + "");
                    rem_sih = totalQty % product.getCaseSize();
                }
                if (product.isBaseUomOuterWise() && product.getOuterSize() != 0) {
                    if (isUomWiseSplitted) {
                        holder.sih_ou.setText(rem_sih / product.getOuterSize() + "");
                        rem_sih = rem_sih % product.getOuterSize();
                    } else {
                        isUomWiseSplitted = true;
                        holder.sih_ou.setText(totalQty / product.getOuterSize() + "");
                        rem_sih = totalQty % product.getOuterSize();
                    }
                }

                if (isUomWiseSplitted) {
                    holder.sih.setText(rem_sih + "");
                } else {
                    holder.sih.setText(product.getStocksih() + "");
                }

            } else {
                holder.sih.setText(product.getStocksih() + "");
            }

            holder.batchnumber.setText(getResources().getString(
                    R.string.batch_no)
                    + ": " + product.getBatchNo() + "");

            return (row);
        }
    }

    class ViewHolder {
        private String pname;
        private TextView psname, sih, batchnumber, sih_cs, sih_ou;
        int ref;
    }


    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void loadStartVisit() {

    }

    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        ArrayList<LoadManagementBO> filterlist = new ArrayList<LoadManagementBO>();
        for (LevelBO levelBO : mParentIdList) {
            for (LoadManagementBO loadMgtBO : mylist) {
                if (levelBO.getProductID() == loadMgtBO.getParentid()) {
                    filterlist.add(loadMgtBO);
                }
            }
        }

        MyAdapter mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        ArrayList<LoadManagementBO> filterlist = new ArrayList<LoadManagementBO>();

        if (mAttributeProducts != null) {
            if (mParentIdList.size() > 0) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : mylist) {
                        if (levelBO.getProductID() == productBO.getParentid()) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(productBO.getProductid())) {
                                mylist.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO loadMgtBO : mylist) {

                        if (pid == loadMgtBO.getProductid()) {
                            mylist.add(loadMgtBO);
                        }
                    }
                }
            }
        } else {

            for (LevelBO levelBO : mParentIdList) {
                for (LoadManagementBO loadMgtBO : mylist) {
                    if (levelBO.getProductID() == loadMgtBO.getParentid()) {
                        filterlist.add(loadMgtBO);
                    }
                }
            }
        }
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        MyAdapter mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);

        //strBarCodeSearch = "ALL";
        //updateValue();
        mDrawerLayout.closeDrawers();
    }

    private void fiveFilterFragment() {
        try {

            Vector<String> vect = new Vector();
            for (String string : getResources().getStringArray(
                    R.array.productFilterArray)) {
                vect.add(string);
            }

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
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<Object>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public void productFilterClickedFragment() {
        try {

            Vector vect = bmodel.productHelper.getChildLevelBo();
            mDrawerLayout.openDrawer(GravityCompat.END);
            // To hide Key Board

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

            Commons.printException(e);
        }
    }

    private void updateTotalSIHValue() {
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.layout_sih);
        rl.setVisibility(View.VISIBLE);

        TextView totalSihTV = (TextView) view.findViewById(R.id.tv_sih);
        int totalSih = 0;

        for (LoadManagementBO loadManagementBO : mylist) {
            if (loadManagementBO.getStocksih() > 0) {
                totalSih = totalSih + loadManagementBO.getStocksih();

            }
        }
        totalSihTV.setText(totalSih + "");
    }

}
