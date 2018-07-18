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

    private static final String OUR_INTENT_ACTION = "com.ivy.cpg.view.van.PlanningSubScreen.RECVR";
    private static final String ACTION_SOFTSCANTRIGGER = "com.motorolasolutions.emdk.datawedge.api.ACTION_SOFTSCANTRIGGER";
    private static final String EXTRA_PARAM = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private static final String DWAPI_TOGGLE_SCANNING = "TOGGLE_SCANNING";
    private static final String MENU_LOAD_MANAGEMENT = "MENU_LOAD_MANAGEMENT";
    private static final HashMap<String, Integer> menuIcons = new HashMap<>();
    private BusinessModel bmodel;
    private boolean isClicked;
    private String menuOdameter = "MENU_ODAMETER";
    private String menuPlanning = "MENU_PLANNING";
    private String menuStockView = "MENU_STOCK_VIEW";
    private String menuVanloadStockView = "MENU_VANLOAD_STOCK_VIEW";

    private String menuManualVanload = "MENU_MANUAL_VAN_LOAD";
    private Intent vanloadstockview;
    private Intent stockViewIntent;
    private Button mSelectedListBTN;
    public String mSelectedBarCodemodule;
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

        menuIcons.put(menuOdameter, R.drawable.icon_odameter);
        menuIcons.put(menuPlanning, R.drawable.icon_order);
        menuIcons.put(menuStockView, R.drawable.icon_stock);
        menuIcons.put(menuVanloadStockView, R.drawable.icon_stock);
        menuIcons.put(menuManualVanload, R.drawable.icon_vanload);
        menuIcons.put(StandardListMasterConstants.MENU_TASK_REPORT, R.drawable.icon_reports);

        Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper.downloadPlanningSubMenu();
        Commons.print("load management," + String.valueOf(menuDB.size()));
        for (int i = 0; i < menuDB.size(); i++)
            Commons.print("menu," + menuDB.get(i).getMenuName());

        ListView listView = (ListView) view.findViewById(R.id.listView1);
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

        if (menuItem.getConfigCode().equals(menuOdameter)) {

            Intent odameterintent = new Intent(getActivity(),
                    OdaMeterScreen.class);
            odameterintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            odameterintent.putExtra("screentitle", menuItem.getMenuName());
            odameterintent.putExtra("planingsub", true);
            startActivity(odameterintent);

        } else if (menuItem.getConfigCode().equals(menuPlanning)) {

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
        } else if (menuItem.getConfigCode().equals(menuVanloadStockView)) {

            if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                checkIsAllowed(
                        menuVanloadStockView,
                        bmodel.configurationMasterHelper.SHOW_VANBARCODE_VALIDATION);
            } else if (bmodel.configurationMasterHelper.SHOW_VANBARCODE_VALIDATION) {
                checkBarCode(menuVanloadStockView);
            } else {
                vanLoadSubRoutine(menuItem.getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(menuStockView)) {
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

        } else if (menuItem.getConfigCode().equals(menuManualVanload)) {

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

    public void checkIsAllowed(String menuString, boolean isValidateBarCode) {
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
                    if (isValidateBarCode)
                        checkBarCode(menuString);
                    else if (menuString.equals(menuVanloadStockView))
                        vanLoadSubRoutine(menuVanloadStockView);

                } else {
                    showToastMessage(distance);
                }
            }
        } catch (Exception e) {
            Commons.printException("checkIsAllowed", e);
        }

    }

    public void checkBarcodeData(Intent i) {
        String mScannedData;
        String mBarCode = "";
        if (i.getAction().contentEquals(OUR_INTENT_ACTION)) {
            mScannedData = i.getStringExtra(DATA_STRING_TAG);
            if (mScannedData == null)
                mScannedData = "";

            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT barcode FROM WarehouseActivityMapping WHERE activity_code = "
                            + DatabaseUtils
                            .sqlEscapeString(mSelectedBarCodemodule));

            if (c != null) {
                if (c.moveToNext()) {
                    mBarCode = c.getString(0);
                }
                c.close();
            }
            db.closeDB();

            if ("".equals(mBarCode))
                showToastMessageForBarcode(-1);
            else if ("".equals(mScannedData))
                showToastMessageForBarcode(-2);
            else if (mScannedData.equals(mBarCode)) {
                if (mSelectedBarCodemodule.equals(menuVanloadStockView))
                    vanLoadSubRoutine(menuVanloadStockView);
            } else
                showToastMessageForBarcode(-3);
        }
    }

    private void showToastMessageForBarcode(int status) {
        String strTitle = "";
        if (status == -1)
            strTitle = getResources().getString(
                    R.string.warehouse_barcode_not_assigned);
        else if (status == -2)
            strTitle = getResources().getString(
                    R.string.not_able_to_scan_barcode);
        else if (status == -3)
            strTitle = getResources().getString(R.string.barcode_not_matched);

        Toast.makeText(getActivity(), strTitle, Toast.LENGTH_SHORT)
                .show();
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

    public void checkBarCode(String menuString) {
        mSelectedBarCodemodule = menuString;

        Intent i = new Intent();
        i.setAction(ACTION_SOFTSCANTRIGGER);
        i.putExtra(EXTRA_PARAM, DWAPI_TOGGLE_SCANNING);
        getActivity().sendBroadcast(i);
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
                bmodel.productHelper
                        .downloadFiveFilterLevels(MENU_LOAD_MANAGEMENT);

                bmodel.productHelper.loadProductsWithFiveLevel(
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
                    bmodel.vanmodulehelper.downloadSubDepots();
                }

                bmodel.productHelper
                        .downloadFiveFilterLevels(MENU_LOAD_MANAGEMENT);

                bmodel.productHelper.loadProductsWithFiveLevel(
                        MENU_LOAD_MANAGEMENT, menuManualVanload);


                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

                    bmodel.productHelper.downlaodReturnableProducts(MENU_LOAD_MANAGEMENT);
                    bmodel.productHelper.downloadBomMaster();
                    bmodel.productHelper.downloadGenericProductID();
                    bmodel.vanmodulehelper.loadVanLoadReturnProductValidation();

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
                holder.menuIcon = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = (TextView) convertView
                        .findViewById(R.id.list_item_menu_tv_loadmgt);

                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mSelectedListBTN != null)
                            mSelectedListBTN.setSelected(false);

//                        mSelectedListBTN = holder.menuBTN;
//                        mSelectedListBTN.setSelected(true);

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
                holder.menuIcon.setImageResource(menuIcons.get(menuPlanning));

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
