package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.FilterFiveFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 11/1/2017,3:18 PM.
 */
public class SalesVolumeReportFragment extends Fragment implements BrandDialogInterface {


    private ListView lvwplist;
    private BusinessModel bmodel;
    private ArrayList<ProductMasterBO> mylist;
    private View view;
    // Drawer Implimentation
    static DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private int mSelectedLastFilterSelection = -1;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_sales_volume_report, container,
                false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        /*((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(
                bmodel.mSelectedActivityName);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(
                R.drawable.icon_sbd);
        ((ActionBarActivity) getActivity()).getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);*/

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                /*((ActionBarActivity) getActivity()).getSupportActionBar()
                        .setTitle(bmodel.mSelectedActivityName);*/
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {

                /*((ActionBarActivity) getActivity()).getSupportActionBar()
                        .setTitle(getResources().getString(R.string.filter));*/
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mSelectedFilterMap.put("Brand", "All");

        lvwplist = (ListView) view.findViewById(R.id.lvwplistorddet);
        lvwplist.setCacheColorHint(0);

        mylist = bmodel.reportHelper.getOrderedProductMaster();
        updateOrderGrid();
        setHasOptionsMenu(true);

        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(
                R.menu.sales_volume_report_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == R.id.menu_filter) {
            //ReportMenuFragmentActivity.mDrawerLayout.closeDrawer(GravityCompat.START);
            productFilterClickedFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            /*android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", "Brand");
            bundle.putString("filterHeader", bmodel.reportHelper.getSellerReportChildLevelBO().get(0).getProductLevel());
            bundle.putString("isFrom", "Survey");
            bundle.putSerializable("serilizeContent",
                    bmodel.reportHelper.getSellerReportChildLevelBO());

            bundle.putBoolean("isFormBrand", false);


            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setBrandDialogInterface(this);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();*/

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            bundle.putString("isFrom", "SVR");
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setBrandDialogInterface(this);
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOrderGrid() {

        // Show alert if error loading data.
        if (mylist == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Show alert if no order exist.
        if (mylist.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_orders_available),
                    Toast.LENGTH_SHORT).show();
            return;
        }


        // Load listview.
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);

    }

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        ArrayList<ProductMasterBO> items;

        private MyAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.row_sales_volume_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;


            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_sales_volume_report, parent, false);
                holder = new ViewHolder();
                holder.PRDNAME = (TextView) row.findViewById(R.id.PRDNAME);

                holder.pdt_total_qty = (TextView) row.findViewById(R.id.pdt_total_qty);
                holder.pdt_total_value = (TextView) row.findViewById(R.id.pdt_total_value);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productMasterBO = items
                    .get(position);

            holder.PRDNAME.setText(holder.productMasterBO.getProductShortName());
            holder.pdt_total_qty.setText(holder.productMasterBO.getTotalQty() + "");
            holder.pdt_total_value.setText(bmodel.getWithoutExponential(holder.productMasterBO.getTotalamount()) + "");

            return (row);
        }
    }

    class ViewHolder {
        ProductMasterBO productMasterBO;
        TextView PRDNAME, pdt_total_qty, pdt_total_value;

    }


    public void onBackPressed() {
        // do something on back.
        return;
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName, List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        mDrawerLayout.closeDrawers();
        mSelectedLastFilterSelection = id;
        updateList(id);
    }

    private void updateList(int id) {
        ArrayList<ProductMasterBO> productMasterBOs = new ArrayList<>();
        if (id != -1) {
            if (mylist != null && mylist.size() > 0) {
                for (int i = 0; i < mylist.size(); i++) {
                    if (mylist.get(i).getParentid() == id) {
                        productMasterBOs.add(mylist.get(i));
                    }
                }
            }
        } else {
            productMasterBOs.addAll(mylist);
        }
        // Load listview.
        MyAdapter mSchedule = new MyAdapter(productMasterBOs);
        lvwplist.setAdapter(mSchedule);
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

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        if (bmodel.isMapEmpty(mSelectedIdByLevelId)) {
            updateList(-1);
        } else if (mParentIdList != null && mParentIdList.size() > 0) {
            updateList(mParentIdList.get(0).getProductID());
        }
        mDrawerLayout.closeDrawers();
    }

}

