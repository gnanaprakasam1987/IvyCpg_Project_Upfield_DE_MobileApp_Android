package com.ivy.sd.png.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.van.ManualVanLoadActivity;
import com.ivy.sd.png.view.van.OdaMeterScreen;
import com.ivy.sd.png.view.van.StockViewActivity;
import com.ivy.sd.png.view.van.VanLoadStockView_activity;

import java.util.HashMap;
import java.util.Vector;

public class PlanningSubScreen extends IvyBaseActivityNoActionBar {
    private static final String OUR_INTENT_ACTION = "com.ivy.sd.png.view.PlanningSubScreen.RECVR";
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
    private String mSelectedBarCodemodule;
    private Intent vanloadintent;

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planningsub);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            checkAndRequestPermissionAtRunTime(3);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(
                    bmodel.configurationMasterHelper.getLoadplanningsubttitle());
            getSupportActionBar().setIcon(R.drawable.icon_stock);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        menuIcons.put(menuOdameter, R.drawable.icon_odameter);
        menuIcons.put(menuPlanning, R.drawable.icon_order);
        menuIcons.put(menuStockView, R.drawable.icon_stock);
        menuIcons.put(menuVanloadStockView, R.drawable.icon_stock);
        menuIcons.put(menuManualVanload, R.drawable.icon_vanload);

        Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper.downloadPlanningSubMenu();
        Commons.print("load management," + String.valueOf(menuDB.size()));
        for (int i = 0; i < menuDB.size(); i++)
            Commons.print("menu," + menuDB.get(i).getMenuName());

        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setCacheColorHint(0);
        listView.setAdapter(new MenuBaseAdapter(menuDB));

    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
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
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    private void gotoNextActivity(ConfigureBO menuItem) {

        String menuPlanningConstant = "Day Planning";
        String menuDashDay = "MENU_DASH_DAY";

        if (menuItem.getConfigCode().equals(menuOdameter)) {

            Intent odameterintent = new Intent(PlanningSubScreen.this,
                    OdaMeterScreen.class);
            odameterintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            odameterintent.putExtra("screentitle", menuItem.getMenuName());
            startActivity(odameterintent);

        } else if (menuItem.getConfigCode().equals(menuPlanning)) {

            if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(this,
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(this, bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (!isClicked) {
                    isClicked = false;
                    Intent i = new Intent(PlanningSubScreen.this,
                            HomeScreenActivity.class);
                    i.putExtra("From", menuPlanningConstant);
                    i.putExtra("Newplanningsub", "Planningsub");
                    bmodel.configurationMasterHelper
                            .setTradecoveragetitle(menuItem.getMenuName());
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

            stockViewIntent = new Intent(PlanningSubScreen.this,
                    StockViewActivity.class);
            stockViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            stockViewIntent.putExtra("screentitle", menuItem.getMenuName());
            new LoadCurrenStock().execute();

        } else if (menuItem.getConfigCode().equals(menuDashDay)) {

            Intent i = new Intent(PlanningSubScreen.this,
                    DashBoardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("screentitle", menuItem.getMenuName());
            i.putExtra("retid", "0");
            i.putExtra("type", "DAY");
            startActivity(i);

        } else if (menuItem.getConfigCode().equals(menuManualVanload)) {

            vanloadintent = new Intent(PlanningSubScreen.this,
                    ManualVanLoadActivity.class);
            vanloadintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            vanloadintent.putExtra("screentitle", menuItem.getMenuName());

            new DownloadManualVanLoad().execute();

        }

    }

    private void vanLoadSubRoutine(String menuName) {
        vanloadstockview = new Intent(PlanningSubScreen.this,
                VanLoadStockView_activity.class);
        vanloadstockview.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanloadstockview.putExtra("screentitle", menuName);
        new DownloadStockViewApply().execute();
    }

    public void checkIsAllowed(String menuString, boolean isValidateBarCode) {
        try {
            DBUtil db = new DBUtil(PlanningSubScreen.this, DataMembers.DB_NAME,
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

    private void checkBarcodeData(Intent i) {
        String mScannedData;
        String mBarCode = "";
        if (i.getAction().contentEquals(OUR_INTENT_ACTION)) {
            mScannedData = i.getStringExtra(DATA_STRING_TAG);
            if (mScannedData == null)
                mScannedData = "";

            DBUtil db = new DBUtil(PlanningSubScreen.this, DataMembers.DB_NAME,
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

        Toast.makeText(PlanningSubScreen.this, strTitle, Toast.LENGTH_SHORT)
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
        Toast.makeText(PlanningSubScreen.this, strTitle, Toast.LENGTH_SHORT)
                .show();
    }

    public void checkBarCode(String menuString) {
        mSelectedBarCodemodule = menuString;

        Intent i = new Intent();
        i.setAction(ACTION_SOFTSCANTRIGGER);
        i.putExtra(EXTRA_PARAM, DWAPI_TOGGLE_SCANNING);
        PlanningSubScreen.this.sendBroadcast(i);
    }

    @Override
    public void onNewIntent(Intent i) {
        try {
            if (i != null && mSelectedBarCodemodule != null) {
                checkBarcodeData(i);
                mSelectedBarCodemodule = null;
            }
        } catch (Exception e) {
            Commons.printException("" + e);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(PlanningSubScreen.this, HomeScreenActivity.class));
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class DownloadManualVanLoad extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(PlanningSubScreen.this);

            customProgressDialog(builder,  getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
                    bmodel.vanmodulehelper.downloadSubDepots();
                }

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels(MENU_LOAD_MANAGEMENT);
                } else {
                    bmodel.productHelper
                            .downloadProductFilter(MENU_LOAD_MANAGEMENT);
                }

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            MENU_LOAD_MANAGEMENT, menuManualVanload);
                else
                    bmodel.productHelper.loadProducts(MENU_LOAD_MANAGEMENT,
                            menuManualVanload);

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

    class LoadCurrenStock extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(PlanningSubScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading_data));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels(MENU_LOAD_MANAGEMENT);
                } else {
                    bmodel.productHelper
                            .downloadProductFilter(MENU_LOAD_MANAGEMENT);
                }

                bmodel.productHelper.loadProducts(MENU_LOAD_MANAGEMENT, "");
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
            builder = new AlertDialog.Builder(PlanningSubScreen.this);
            customProgressDialog(builder,  getResources().getString(R.string.loading));
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

                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_menu, parent,
                        false);
                holder = new ViewHolder();
                holder.menuIcon = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = (TextView) convertView
                        .findViewById(R.id.list_item_menu_tv_new);

                convertView.setOnClickListener(new OnClickListener() {

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
                holder.menuIcon.setImageResource(menuIcons.get(menuPlanning));
            return convertView;
        }

        class ViewHolder {
            private ConfigureBO config;
            private ImageView menuIcon;
            private TextView menuBTN;
        }
    }

}