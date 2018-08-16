package com.ivy.cpg.view.van;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.van.vanstockapply.VanLoadStockApplyActivity;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.dashboard.olddashboard.DashBoardActivity;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.PlanningVisitActivity;
import com.ivy.cpg.view.reports.ReportActivity;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by hanifa.m on 5/3/2017.
 */

public class PlanningSubScreenFragment extends IvyBaseFragment {


    private static final String MENU_LOAD_MANAGEMENT = "MENU_LOAD_MANAGEMENT";

    private static final HashMap<String, Integer> menuIcons = new HashMap<>();

    private BusinessModel bmodel;
    private boolean isClicked;

    private static final String MENU_PLANNING = "MENU_PLANNING";
    private static final String MENU_ODAMETER = "MENU_ODAMETER";
    private static final String MENU_STOCK_VIEW = "MENU_STOCK_VIEW";
    private static final String MENU_VANLOAD_STOCK_VIEW = "MENU_VANLOAD_STOCK_VIEW";
    private static final String MENU_MANUAL_VAN_LOAD = "MENU_MANUAL_VAN_LOAD";

    private Intent vanloadstockview;
    private Intent stockViewIntent;
    private Intent vanloadintent;

    private View view;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_load_management, container, false);


        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            setScreenTitle(bmodel.configurationMasterHelper.getLoadplanningsubttitle());

        }

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            ((HomeScreenActivity) getActivity()).checkAndRequestPermissionAtRunTime(3);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        menuIcons.put(MENU_ODAMETER, R.drawable.icon_odameter);
        menuIcons.put(MENU_ODAMETER, R.drawable.icon_order);
        menuIcons.put(MENU_STOCK_VIEW, R.drawable.icon_stock);
        menuIcons.put(MENU_VANLOAD_STOCK_VIEW, R.drawable.icon_stock);
        menuIcons.put(MENU_MANUAL_VAN_LOAD, R.drawable.icon_vanload);
        menuIcons.put(StandardListMasterConstants.MENU_TASK_REPORT, R.drawable.icon_reports);

        Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper.downloadPlanningSubMenu();

        ListView listView = view.findViewById(R.id.listView1);
        listView.setCacheColorHint(0);
        listView.setAdapter(new MenuBaseAdapter(menuDB));

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
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view -view
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

    private void gotoNextActivity(ConfigureBO menuItem) {

        String menuPlanningConstant = "Day Planning";
        String menuDashDay = "MENU_DASH_DAY";

        if (menuItem.getConfigCode().equals(MENU_ODAMETER)) {

            Intent odameterintent = new Intent(getActivity(),
                    OdaMeterScreen.class);
            odameterintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            odameterintent.putExtra("screentitle", menuItem.getMenuName());
            startActivity(odameterintent);

        } else if (menuItem.getConfigCode().equals(MENU_PLANNING)) {

            if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (!isClicked) {
                    isClicked = false;
                    bmodel.distributorMasterHelper.downloadDistributorsList();
                    bmodel.configurationMasterHelper
                            .setTradecoveragetitle(menuItem.getMenuName());
                    Intent i = new Intent(getActivity(),
                            PlanningVisitActivity.class);
                    i.putExtra("isPlanningSub", true);
                    startActivity(i);
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_VANLOAD_STOCK_VIEW)) {

            if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                checkIsAllowed(
                        MENU_VANLOAD_STOCK_VIEW);
            }  else {
                vanLoadSubRoutine(menuItem.getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_STOCK_VIEW)) {
            bmodel.configurationMasterHelper.loadStockUOMConfiguration();
            stockViewIntent = new Intent(getActivity(),
                    StockViewActivity.class);
            stockViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            stockViewIntent.putExtra("screentitle", menuItem.getMenuName());
            stockViewIntent.putExtra("planingsub", true);
            new LoadCurrenStock().execute();

        } else if (menuItem.getConfigCode().equals(menuDashDay)) {

            Intent i = new Intent(getActivity(),
                    DashBoardActivity.class);
            i.putExtra("screentitle", menuItem.getMenuName());
            startActivity(i);

        } else if (menuItem.getConfigCode().equals(MENU_MANUAL_VAN_LOAD)) {

            vanloadintent = new Intent(getActivity(),
                    ManualVanLoadActivity.class);
            vanloadintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            vanloadintent.putExtra("screentitle", menuItem.getMenuName());
            vanloadintent.putExtra("planingsub", true);
            new DownloadManualVanLoad().execute();

        } else if (menuItem.getConfigCode().equals(StandardListMasterConstants.MENU_TASK_REPORT)) {

            ConfigureBO configureBO = new ConfigureBO();
            configureBO.setMenuName(menuItem.getMenuName());
            configureBO.setConfigCode(StandardListMasterConstants.MENU_TASK_REPORT);

            Intent intent = new Intent(getActivity(), ReportActivity.class);
            Bundle bun = new Bundle();
            bun.putSerializable("config", configureBO);
            bun.putString("FROM", "LOADMANAGEMENT");
            intent.putExtras(bun);

            startActivity(intent);
        }

    }

    public void checkIsAllowed(String menuString) {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT latitude, longitude FROM WarehouseActivityMapping WHERE activity_code = "
                            + DatabaseUtils.sqlEscapeString(menuString));
            double wareLatitude = 0;
            double wareLongitude = 0;
            if (c != null) {
                if (c.moveToNext()) {
                    wareLatitude = c.getDouble(0);
                    wareLongitude = c.getDouble(1);
                }
                c.close();
            }
            db.closeDB();

            if (wareLatitude == 0 && wareLongitude == 0) {
                showToastMessage(-1);
            } else if (LocationUtil.latitude == 0
                    && LocationUtil.longitude == 0) {
                showToastMessage(-2);
            } else {

                float distance = LocationUtil.calculateDistance(wareLatitude,
                        wareLongitude);
                if (distance <= ConfigurationMasterHelper.vanDistance) {
                    if (menuString.equals(MENU_VANLOAD_STOCK_VIEW))
                        vanLoadSubRoutine(MENU_VANLOAD_STOCK_VIEW);

                } else {
                    showToastMessage(distance);
                }
            }
        } catch (Exception e) {
            Commons.printException("checkIsAllowed", e);
        }

    }

    private void showToastMessage(float distance) {
        String strTitle;
        if (distance == -1)
            strTitle = getResources().getString(
                    R.string.warehouse_location_not_assigned);
        else if (distance == -2)
            strTitle = getResources().getString(
                    R.string.not_able_to_find_user_location);
        else
            strTitle = getResources().getString(R.string.you_are) + " "
                    + distance + getResources().getString(R.string.mts_away);
        Toast.makeText(getActivity(), strTitle, Toast.LENGTH_SHORT)
                .show();
    }



    private void vanLoadSubRoutine(String menuName) {
        vanloadstockview = new Intent(getActivity(),
                VanLoadStockApplyActivity.class);
        vanloadstockview.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanloadstockview.putExtra("screentitle", menuName);
        vanloadstockview.putExtra("planingsub", true);
        new DownloadStockViewApply().execute();
    }

    class LoadCurrenStock extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading_data));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_LOAD_MANAGEMENT));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(MENU_LOAD_MANAGEMENT,
                        bmodel.productHelper.getFilterProductLevels()));

                bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");

            } catch (Exception e) {
                Commons.printException(" + e");
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            try {
                if (alertDialog != null)
                    alertDialog.dismiss();
            } catch (Exception e) {
                Commons.printException(" + e");
            }
            startActivity(stockViewIntent);
        }

    }

    class DownloadStockViewApply extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.configurationMasterHelper.downloadSIHAppliedById();
                bmodel.stockreportmasterhelper.downloadStockReportMaster();
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            startActivity(vanloadstockview);
        }

    }


    class DownloadManualVanLoad extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
                    bmodel.loadManagementHelper.downloadSubDepots();
                }

                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_LOAD_MANAGEMENT));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(MENU_LOAD_MANAGEMENT,
                        bmodel.productHelper.getFilterProductLevels()));

                bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                        MENU_LOAD_MANAGEMENT, MENU_MANUAL_VAN_LOAD);


                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

                    bmodel.productHelper.downlaodReturnableProducts(MENU_LOAD_MANAGEMENT);
                    bmodel.productHelper.downloadBomMaster();
                    bmodel.productHelper.downloadGenericProductID();
                    bmodel.loadManagementHelper.loadVanLoadReturnProductValidation();

                }

                OrderHeader ordHeadBO = new OrderHeader();
                bmodel.setOrderHeaderBO(ordHeadBO);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            startActivity(vanloadintent);

        }

    }


    class MenuBaseAdapter extends BaseAdapter {

        Vector<ConfigureBO> items;

        public MenuBaseAdapter(Vector<ConfigureBO> menuDB) {
            this.items = menuDB;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ConfigureBO configTemp = items.get(position);
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.custom_newui_list_item, parent,
                        false);
                holder = new ViewHolder();
                holder.menuIcon = convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = convertView
                        .findViewById(R.id.list_item_menu_tv_loadmgt);

                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        gotoNextActivity(holder.config);
                    }
                });


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.config = configTemp;
            holder.menuBTN.setText(configTemp.getMenuName());
            holder.menuBTN.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            Integer i = menuIcons.get(configTemp.getConfigCode());
            if (i != null)
                holder.menuIcon.setImageResource(i);
            else
                holder.menuIcon.setImageResource(menuIcons.get(MENU_PLANNING));

            if (position % 2 == 0)
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.history_list_bg));

            return convertView;
        }

        class ViewHolder {
            private ConfigureBO config;
            private ImageView menuIcon;
            private TextView menuBTN;
        }
    }

}
