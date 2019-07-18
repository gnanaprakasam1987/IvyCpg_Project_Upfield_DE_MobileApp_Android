package com.ivy.cpg.view.reports.slaesvolumereport;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dharmapriya.k on 11/1/2017,3:18 PM.
 */
public class SalesVolumeReportFragment extends Fragment implements BrandDialogInterface, FiveLevelFilterCallBack {


    private ListView lvwplist;
    private BusinessModel bmodel;
    private ArrayList<SalesVolumeBo> mylist;
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

        FrameLayout drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

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


        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                getActivity().supportInvalidateOptionsMenu();

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

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
            view.findViewById(R.id.total_weight).setVisibility(View.GONE);

        lvwplist = view.findViewById(R.id.lvwplistorddet);
        lvwplist.setCacheColorHint(0);

        SalesVolumeReportHelper reportHelper = SalesVolumeReportHelper.getInstance(getActivity());
        reportHelper.downloadProductReportsWithFiveLevelFilter();
        mylist = reportHelper.getOrderedProductMaster();

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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_filter).setVisible(!drawerOpen);

        if (mylist == null || mylist.size() == 0) {
            menu.findItem(R.id.menu_filter).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == R.id.menu_filter) {
            //ReportMenuFragmentActivity.mDrawerLayout.closeDrawer(GravityCompat.START);
            fiveFilterClickedFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackButtonClick() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
            mDrawerLayout.closeDrawers();
        else {
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void fiveFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            FragmentManager fm = getActivity().getSupportFragmentManager();
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

    class MyAdapter extends ArrayAdapter<SalesVolumeBo> {
        ArrayList<SalesVolumeBo> items;

        private MyAdapter(ArrayList<SalesVolumeBo> items) {
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
                holder.PRDNAME = row.findViewById(R.id.prd_nameTv);

                holder.pdt_total_qty = row.findViewById(R.id.pdt_total_qty);
                holder.pdt_total_value = row.findViewById(R.id.pdt_total_value);
                holder.pdt_total_weight = row.findViewById(R.id.pdt_total_weight);

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                    holder.pdt_total_weight.setVisibility(View.GONE);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productMasterBO = items
                    .get(position);

            holder.PRDNAME.setText(holder.productMasterBO.getProductShortName());
            holder.pdt_total_qty.setText(holder.productMasterBO.getTotalQty() + "");
            holder.pdt_total_value.setText(SDUtil.getWithoutExponential(holder.productMasterBO.getTotalamount()) + "");
            holder.pdt_total_weight.setText(Utils.formatAsTwoDecimal((double) holder.productMasterBO.getTotalWeight()*holder.productMasterBO.getTotalQty()));

            return (row);
        }
    }

    class ViewHolder {
        SalesVolumeBo productMasterBO;
        TextView PRDNAME, pdt_total_qty, pdt_total_value, pdt_total_weight;

    }


    public void onBackPressed() {
        // do something on back.
        return;
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        mDrawerLayout.closeDrawers();
        mSelectedLastFilterSelection = id;
        updateList(id);
    }

    private void updateList(int id) {
        ArrayList<SalesVolumeBo> productMasterBOs = new ArrayList<>();
        if (id != -1) {
            if (mylist != null && mylist.size() > 0) {
                for (int i = 0; i < mylist.size(); i++) {
                    if (mylist.get(i).getParentHierarchy().contains("/" + id + "/")) {
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
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        if (AppUtils.isMapEmpty(mSelectedIdByLevelId)) {
            updateList(-1);
        } else
            updateList(mProductId);

        mDrawerLayout.closeDrawers();
    }

}

